/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Complete implementation of the
 * {@link org.springframework.beans.factory.support.AutowireCandidateResolver} strategy
 * interface, providing support for qualifier annotations as well as for lazy resolution
 * driven by the {@link Lazy} annotation in the {@code context.annotation} package.
 * <p>
 * 官方把这个类描述为：策略接口的完整实现。
 * 它不仅仅支持上面所有描述的功能，还支持@Lazy懒处理~~~(注意此处懒处理(延迟处理)，不是懒加载~)
 *
 * @author Juergen Hoeller
 * @Lazy 一般含义是懒加载，它只会作用于BeanDefinition.setLazyInit()。而此处给它增加了一个能力：延迟处理（代理处理）
 * @since 4.0
 */
public class ContextAnnotationAutowireCandidateResolver extends QualifierAnnotationAutowireCandidateResolver {

	/**
	 * // 这是此类本身唯一做的事，此处精析
	 * // 返回该 lazy proxy 表示延迟初始化，实现过程是查看在 @Autowired 注解处是否使用了 @Lazy = true 注解
	 *
	 * @param descriptor
	 * @param beanName
	 * @return
	 */
	@Override
	@Nullable
	public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, @Nullable String beanName) {

		// 如果isLazy=true  那就返回一个代理，否则返回null
		// 相当于若标注了@Lazy注解，就会返回一个代理（当然@Lazy注解的value值不能是false）
		return (isLazy(descriptor) ? buildLazyResolutionProxy(descriptor, beanName) : null);
	}

	/**
	 * // 这个比较简单，@Lazy注解标注了就行（value属性默认值是true）
	 * // @Lazy支持标注在属性上和方法入参上~~~  这里都会解析
	 *
	 * @param descriptor
	 * @return
	 */
	protected boolean isLazy(DependencyDescriptor descriptor) {
		for (Annotation ann : descriptor.getAnnotations()) {
			Lazy lazy = AnnotationUtils.getAnnotation(ann, Lazy.class);
			if (lazy != null && lazy.value()) {
				return true;
			}
		}
		MethodParameter methodParam = descriptor.getMethodParameter();
		if (methodParam != null) {
			Method method = methodParam.getMethod();
			if (method == null || void.class == method.getReturnType()) {
				Lazy lazy = AnnotationUtils.getAnnotation(methodParam.getAnnotatedElement(), Lazy.class);
				if (lazy != null && lazy.value()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * // 核心内容，是本类的灵魂~~~
	 *
	 * @param descriptor
	 * @param beanName
	 * @return
	 */
	protected Object buildLazyResolutionProxy(final DependencyDescriptor descriptor, final @Nullable String beanName) {
		Assert.state(getBeanFactory() instanceof DefaultListableBeanFactory, "BeanFactory needs to be a DefaultListableBeanFactory");

		// 这里毫不客气的使用了面向实现类编程，使用了DefaultListableBeanFactory.doResolveDependency()方法~~~
		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) getBeanFactory();

		//TargetSource 是它实现懒加载的核心原因，在AOP那一章节了重点提到过这个接口，此处不再叙述
		// 它有很多的著名实现如HotSwappableTargetSource、SingletonTargetSource、LazyInitTargetSource、
		//SimpleBeanTargetSource、ThreadLocalTargetSource、PrototypeTargetSource等等非常多
		// 此处因为只需要自己用，所以采用匿名内部类的方式实现~~~ 此处最重要是看getTarget方法，它在被使用的时候（也就是代理对象真正使用的时候执行~~~）
		TargetSource ts = new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return descriptor.getDependencyType();
			}

			@Override
			public boolean isStatic() {
				return false;
			}

			/**
			 * // getTarget是调用代理方法的时候会调用的，所以执行每个代理方法都会执行此方法，这也是为何doResolveDependency
			 * 	// 我个人认为它在效率上，是存在一定的问题的~~~所以此处建议尽量少用@Lazy~~~
			 * 	//不过效率上应该还好，对比http、序列化反序列化处理，简直不值一提  所以还是无所谓  用吧
			 * @return
			 */
			@Override
			public Object getTarget() {
				Object target = beanFactory.doResolveDependency(descriptor, beanName, null, null);
				if (target == null) {
					Class<?> type = getTargetClass();
					if (Map.class == type) {
						return Collections.emptyMap();
					} else if (List.class == type) {
						return Collections.emptyList();
					} else if (Set.class == type || Collection.class == type) {
						return Collections.emptySet();
					}
					throw new NoSuchBeanDefinitionException(descriptor.getResolvableType(),
							"Optional dependency not present for lazy injection point");
				}
				return target;
			}

			@Override
			public void releaseTarget(Object target) {
			}
		};

		// 使用ProxyFactory  给ts生成一个代理
		// 由此可见最终生成的代理对象的目标对象其实是TargetSource,而TargetSource的目标才是我们业务的对象
		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(ts);
		Class<?> dependencyType = descriptor.getDependencyType();

		// 如果注入的语句是这么写的private AInterface a;  那这类就是借口 值是true
		// 把这个接口类型也得放进去（不然这个代理都不属于这个类型，反射set的时候岂不直接报错了吗？？？？）
		if (dependencyType.isInterface()) {
			pf.addInterface(dependencyType);
		}
		return pf.getProxy(beanFactory.getBeanClassLoader());
	}

}
