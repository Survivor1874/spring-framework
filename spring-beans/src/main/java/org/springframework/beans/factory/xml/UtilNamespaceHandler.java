/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

/**
 * {@link NamespaceHandler} for the {@code util} namespace.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	private static final String SCOPE_ATTRIBUTE = "scope";


	/**
	 * 可以看到这个 namespaceHandler 解析 constant、list、set、map、properties 集合。
	 */
	@Override
	public void init() {
		registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
		registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
		registerBeanDefinitionParser("list", new ListBeanDefinitionParser());
		registerBeanDefinitionParser("set", new SetBeanDefinitionParser());
		registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
	}


	private static class ConstantBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return FieldRetrievingFactoryBean.class;
		}

		@Override
		protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute("static-field");
			}
			return id;
		}
	}


	private static class PropertyPathBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return PropertyPathFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			String path = element.getAttribute("path");
			if (!StringUtils.hasText(path)) {
				parserContext.getReaderContext().error("Attribute 'path' must not be empty", element);
				return;
			}
			int dotIndex = path.indexOf('.');
			if (dotIndex == -1) {
				parserContext.getReaderContext().error(
						"Attribute 'path' must follow pattern 'beanName.propertyName'", element);
				return;
			}
			String beanName = path.substring(0, dotIndex);
			String propertyPath = path.substring(dotIndex + 1);
			builder.addPropertyValue("targetBeanName", beanName);
			builder.addPropertyValue("propertyPath", propertyPath);
		}

		@Override
		protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute("path");
			}
			return id;
		}
	}


	private static class ListBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return ListFactoryBean.class;
		}

		/**
		 * 解析list-class标签和这个标签的targetListClass属性最后添加到BeanDefinitionBuilder中。
		 *
		 * @param element       the XML element being parsed
		 * @param parserContext the object encapsulating the current state of the parsing process
		 * @param builder       used to define the {@code BeanDefinition}
		 */
		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			List<Object> parsedList = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceList", parsedList);

			//  解析list-class标签
			String listClass = element.getAttribute("list-class");
			if (StringUtils.hasText(listClass)) {
				builder.addPropertyValue("targetListClass", listClass);
			}

			//  获取scope属性值
			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}


	private static class SetBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return SetFactoryBean.class;
		}

		/**
		 * 解析set-class标签和targetSetClass属性添加到BeanDefinitionBuilder中。
		 *
		 * @param element       the XML element being parsed
		 * @param parserContext the object encapsulating the current state of the parsing process
		 * @param builder       used to define the {@code BeanDefinition}
		 */
		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			Set<Object> parsedSet = parserContext.getDelegate().parseSetElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceSet", parsedSet);

			String setClass = element.getAttribute("set-class");
			if (StringUtils.hasText(setClass)) {
				builder.addPropertyValue("targetSetClass", setClass);
			}

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}


	private static class MapBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return MapFactoryBean.class;
		}

		/**
		 * 解析map-class标签和targetMapClass属性添加到BeanDefinitionBuilder中。
		 *
		 * @param element       the XML element being parsed
		 * @param parserContext the object encapsulating the current state of the parsing process
		 * @param builder       used to define the {@code BeanDefinition}
		 */
		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			Map<Object, Object> parsedMap = parserContext.getDelegate().parseMapElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceMap", parsedMap);

			String mapClass = element.getAttribute("map-class");
			if (StringUtils.hasText(mapClass)) {
				builder.addPropertyValue("targetMapClass", mapClass);
			}

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}


	private static class PropertiesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return PropertiesFactoryBean.class;
		}

		/**
		 * 解析 location 属性，properties 文件的路径，文件可以有多个用,分开，
		 * 把 locations 属性值是 properties 文件数组添加到 BeanDefinitionBuilder 中，
		 * 添加 BeanDefinitionBuilder ignoreResourceNotFound 属性，也就是配置的 ignore-resource-not-found 参数值，
		 * 没找到属性文件是否忽略。添加 BeanDefinitionBuilder 的 localOverride 属性，local-override 参数值，
		 * 属性值重复了是否可以覆盖。
		 *
		 * @param element       the XML element being parsed
		 * @param parserContext the object encapsulating the current state of the parsing process
		 * @param builder       used to define the {@code BeanDefinition}
		 */
		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			Properties parsedProps = parserContext.getDelegate().parsePropsElement(element);
			builder.addPropertyValue("properties", parsedProps);

			String location = element.getAttribute("location");
			if (StringUtils.hasLength(location)) {
				location = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(location);
				String[] locations = StringUtils.commaDelimitedListToStringArray(location);
				builder.addPropertyValue("locations", locations);
			}

			builder.addPropertyValue("ignoreResourceNotFound",
					Boolean.valueOf(element.getAttribute("ignore-resource-not-found")));

			builder.addPropertyValue("localOverride",
					Boolean.valueOf(element.getAttribute("local-override")));

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}

}
