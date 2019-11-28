/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by bean metadata elements
 * that carry a configuration source object.
 * spring ioc 设计者把 bean 的初始化过程分为 namespaceHandler 元数据装载，
 * BeanDefinition 在 bean 初始化之前的元数据，然后把把 BeanDefinition 注册到 BeanFactory 中，
 * 然后 BeanPostProcessor 是在 BeanDefinition 创建过程到 bean 初始化过程进行干预的实现，
 * aware 对 bean 初始化过程进行干预的实现
 * <p>
 * BeanDefinition 的顶层接口
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface BeanMetadataElement {

	/**
	 * Return the configuration source {@code Object} for this metadata element
	 * (may be {@code null}).
	 * <p>
	 * 返回元素的配置对象。
	 */
	@Nullable
	Object getSource();

}
