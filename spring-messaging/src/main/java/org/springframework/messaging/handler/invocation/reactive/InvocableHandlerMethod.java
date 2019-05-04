/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.messaging.handler.invocation.reactive;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import reactor.core.publisher.Mono;

import org.springframework.core.CoroutinesUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Extension of {@link HandlerMethod} that invokes the underlying method with
 * argument values resolved from the current HTTP request through a list of
 * {@link HandlerMethodArgumentResolver}.
 *
 * @author Rossen Stoyanchev
 * @since 5.2
 */
public class InvocableHandlerMethod extends HandlerMethod {

	private static final Mono<Object[]> EMPTY_ARGS = Mono.just(new Object[0]);

	private static final Object NO_ARG_VALUE = new Object();


	private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	private ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();


	/**
	 * Create an instance from a {@code HandlerMethod}.
	 */
	public InvocableHandlerMethod(HandlerMethod handlerMethod) {
		super(handlerMethod);
	}

	/**
	 * Create an instance from a bean instance and a method.
	 */
	public InvocableHandlerMethod(Object bean, Method method) {
		super(bean, method);
	}


	/**
	 * Configure the argument resolvers to use to use for resolving method
	 * argument values against a {@code ServerWebExchange}.
	 */
	public void setArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		this.resolvers.addResolvers(resolvers);
	}

	/**
	 * Return the configured argument resolvers.
	 */
	public List<HandlerMethodArgumentResolver> getResolvers() {
		return this.resolvers.getResolvers();
	}

	/**
	 * Set the ParameterNameDiscoverer for resolving parameter names when needed
	 * (e.g. default request attribute name).
	 * <p>Default is a {@link DefaultParameterNameDiscoverer}.
	 */
	public void setParameterNameDiscoverer(ParameterNameDiscoverer nameDiscoverer) {
		this.parameterNameDiscoverer = nameDiscoverer;
	}

	/**
	 * Return the configured parameter name discoverer.
	 */
	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return this.parameterNameDiscoverer;
	}

	/**
	 * Configure a reactive registry. This is needed for cases where the response
	 * is fully handled within the controller in combination with an async void
	 * return value.
	 * <p>By default this is an instance of {@link ReactiveAdapterRegistry} with
	 * default settings.
	 * @param registry the registry to use
	 */
	public void setReactiveAdapterRegistry(ReactiveAdapterRegistry registry) {
		this.reactiveAdapterRegistry = registry;
	}


	/**
	 * Invoke the method for the given exchange.
	 * @param message the current message
	 * @param providedArgs optional list of argument values to match by type
	 * @return a Mono with the result from the invocation.
	 */
	@SuppressWarnings("KotlinInternalInJava")
	public Mono<Object> invoke(Message<?> message, Object... providedArgs) {

		return getMethodArgumentValues(message, providedArgs).flatMap(args -> {
			Object value;
			try {
				Method method = getBridgedMethod();
				ReflectionUtils.makeAccessible(method);
				if (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(method.getDeclaringClass())) {
					value = CoroutinesUtils.invokeHandlerMethod(method, getBean(), args);
				}
				else {
					value = method.invoke(getBean(), args);
				}
			}
			catch (IllegalArgumentException ex) {
				assertTargetBean(getBridgedMethod(), getBean(), args);
				String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
				return Mono.error(new IllegalStateException(formatInvokeError(text, args), ex));
			}
			catch (InvocationTargetException ex) {
				return Mono.error(ex.getTargetException());
			}
			catch (Throwable ex) {
				// Unlikely to ever get here, but it must be handled...
				return Mono.error(new IllegalStateException(formatInvokeError("Invocation failure", args), ex));
			}

			MethodParameter returnType = getReturnType();
			ReactiveAdapter adapter = this.reactiveAdapterRegistry.getAdapter(returnType.getParameterType());
			return isAsyncVoidReturnType(returnType, adapter) ?
					Mono.from(adapter.toPublisher(value)) : Mono.justOrEmpty(value);
		});
	}

	private Mono<Object[]> getMethodArgumentValues(Message<?> message, Object... providedArgs) {
		if (ObjectUtils.isEmpty(getMethodParameters())) {
			return EMPTY_ARGS;
		}
		MethodParameter[] parameters = getMethodParameters();
		List<Mono<Object>> argMonos = new ArrayList<>(parameters.length);
		for (MethodParameter parameter : parameters) {
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			Object providedArg = findProvidedArgument(parameter, providedArgs);
			if (providedArg != null) {
				argMonos.add(Mono.just(providedArg));
				continue;
			}
			if (!this.resolvers.supportsParameter(parameter)) {
				return Mono.error(new MethodArgumentResolutionException(
						message, parameter, formatArgumentError(parameter, "No suitable resolver")));
			}
			try {
				argMonos.add(this.resolvers.resolveArgument(parameter, message)
						.defaultIfEmpty(NO_ARG_VALUE)
						.doOnError(cause -> logArgumentErrorIfNecessary(parameter, cause)));
			}
			catch (Exception ex) {
				logArgumentErrorIfNecessary(parameter, ex);
				argMonos.add(Mono.error(ex));
			}
		}
		return Mono.zip(argMonos, values ->
				Stream.of(values).map(o -> o != NO_ARG_VALUE ? o : null).toArray());
	}

	private void logArgumentErrorIfNecessary(MethodParameter parameter, Throwable cause) {
		// Leave stack trace for later, if error is not handled..
		String causeMessage = cause.getMessage();
		if (!causeMessage.contains(parameter.getExecutable().toGenericString())) {
			if (logger.isDebugEnabled()) {
				logger.debug(formatArgumentError(parameter, causeMessage));
			}
		}
	}

	private boolean isAsyncVoidReturnType(MethodParameter returnType, @Nullable ReactiveAdapter reactiveAdapter) {
		if (reactiveAdapter != null && reactiveAdapter.supportsEmpty()) {
			if (reactiveAdapter.isNoValue()) {
				return true;
			}
			Type parameterType = returnType.getGenericParameterType();
			if (parameterType instanceof ParameterizedType) {
				ParameterizedType type = (ParameterizedType) parameterType;
				if (type.getActualTypeArguments().length == 1) {
					return Void.class.equals(type.getActualTypeArguments()[0]);
				}
			}
		}
		return false;
	}

}