/**
 * Author:   Lynn
 * Date:     2022/3/29 9:17
 */
package com.wu.selfEdit;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * @author Lynn
 * @create 2022/3/29
 */
public class AddressPropertyEditorRegistrar implements PropertyEditorRegistrar {

	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		registry.registerCustomEditor(Address.class, new AddressPropertyEditor());
	}
}