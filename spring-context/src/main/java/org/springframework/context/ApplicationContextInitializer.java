/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.context;

/**
 * Callback interface for initializing a Spring {@link ConfigurableApplicationContext}
 * prior to being {@linkplain ConfigurableApplicationContext#refresh() refreshed}.
 * <p>
 * 用于在刷新容器之前初始化Spring的回调接口。
 * ApplicationContextInitializer是Spring框架原有的概念,
 * 这个类的主要目的就是在 ConfigurableApplicationContext 类型（或者子类型）的ApplicationContext进行刷新refresh之前，
 * 允许我们对 ConfigurableApplicationContext 的实例做进一步的设置或者处理。
 * <p>
 * 通常用于需要对应用程序进行某些初始化工作的 web 程序中。例如利用 Environment 上下文环境注册属性源、激活配置文件等等。
 * 另外它支持Ordered和@Order方式排序执行~
 * <p>
 * ApplicationContextInitializer是Spring留出来允许我们在上下文刷新之前做自定义操作的钩子，
 * 若我们有需求想要深度整合Spring上下文，借助它不乏是一个非常好的实现
 *
 * <p>Typically used within web applications that require some programmatic initialization
 * of the application context. For example, registering property sources or activating
 * profiles against the {@linkplain ConfigurableApplicationContext#getEnvironment()
 * context's environment}. See {@code ContextLoader} and {@code FrameworkServlet} support
 * for declaring a "contextInitializerClasses" context-param and init-param, respectively.
 *
 * <p>{@code ApplicationContextInitializer} processors are encouraged to detect
 * whether Spring's {@link org.springframework.core.Ordered Ordered} interface has been
 * implemented or if the @{@link org.springframework.core.annotation.Order Order}
 * annotation is present and to sort instances accordingly if so prior to invocation.
 *
 * @param <C> the application context type
 * @author Chris Beams
 * @see org.springframework.web.context.ContextLoader#customizeContext
 * @see org.springframework.web.context.ContextLoader#CONTEXT_INITIALIZER_CLASSES_PARAM
 * @see org.springframework.web.servlet.FrameworkServlet#setContextInitializerClasses
 * @see org.springframework.web.servlet.FrameworkServlet#applyInitializers
 * @since 3.1
 */
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {

	/**
	 * Initialize the given application context.
	 * 此接口，Spring Framework 自己没有提供任何的实现类。SpringBoot对它有较多的扩展实现。
	 *
	 * @param applicationContext the application to configure
	 */
	void initialize(C applicationContext);

}
