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

package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Standalone XML application context, taking the context definition files
 * from the file system or from URLs, interpreting plain paths as relative
 * file system locations (e.g. "mydir/myfile.txt"). Useful for test harnesses
 * as well as for standalone environments.
 *
 * <p><b>NOTE:</b> Plain paths will always be interpreted as relative
 * to the current VM working directory, even if they start with a slash.
 * (This is consistent with the semantics in a Servlet container.)
 * <b>Use an explicit "file:" prefix to enforce an absolute file path.</b>
 *
 * <p>The config location defaults can be overridden via {@link #getConfigLocations},
 * Config locations can either denote concrete files like "/myfiles/context.xml"
 * or Ant-style patterns like "/myfiles/*-context.xml" (see the
 * {@link org.springframework.util.AntPathMatcher} javadoc for pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p><b>This is a simple, one-stop shop convenience ApplicationContext.
 * Consider using the {@link GenericApplicationContext} class in combination
 * with an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}
 * for more flexible context setup.</b>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 * @see GenericApplicationContext
 */
// 这个类是一个支持xml定义BeanDefinition的Application，并且可以指定以文件形式的BeanDefinition的读入，这些文件可以使用文件路径和URL来表示
// 在测试环境和独立应用环境中，这个Application是非常有用的。
// 从定位关系图Hierarchy 可以清楚的看到整个BeanDefinition资源定位的过程，这个对BeanDefinition资源定位的过程，最初就是由refresh来出发的。
// refresh的调用是在构造函数中启动的。
// 疑问点？
// 1.FileSystemXmlApplicationContext在什么地方定义了BeanDefinition的读入器BeanDefinitionReader，从而完成BeanDefinition信息的读入呢？
// 答案：在ioc容器的初始化过程中，BeanDefinition资源定位、读入、注册过程是分开进行的，解耦的体现。可以到	FileSystemXmlApplicationContext的基类中
// AbstractRefreshableConfigApplicationContext可以从这个了中来看具体的实现
// 因为从调用链路来看，主要是refresh来进行触发ioc容器进行初始化的，然后进行的步骤是 资源定位-->载入-->注册到ioc容器中
// refresh方法中调用的obtainFreshBeanFactory方法，然后调用AbstractRefreshableConfigApplicationContext的refreshBeanFactory来进行的读入资源
// 定位资源主要步骤refreshBeanFactory-->AbstractRefreshableConfigApplicationContext中-->FileSystemResource的getResourceByPath()-->返回文件系统资源
public class FileSystemXmlApplicationContext extends AbstractXmlApplicationContext {

	/**
	 * Create a new FileSystemXmlApplicationContext for bean-style configuration.
	 * @see #setConfigLocation
	 * @see #setConfigLocations
	 * @see #afterPropertiesSet()
	 */
	public FileSystemXmlApplicationContext() {
	}

	/**
	 * Create a new FileSystemXmlApplicationContext for bean-style configuration.
	 * @param parent the parent context
	 * @see #setConfigLocation
	 * @see #setConfigLocations
	 * @see #afterPropertiesSet()
	 */
	public FileSystemXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML file and automatically refreshing the context.
	 * @param configLocation file path
	 * @throws BeansException if context creation failed
	 */
	// 这个构造函数允许configLocation包含的是BeanDefinition所在的路径
	public FileSystemXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation}, true, null);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML files and automatically refreshing the context.
	 * @param configLocations array of file paths
	 * @throws BeansException if context creation failed
	 */
	// 这个构造函数允许configLocation包含多个BeanDefinition的文件路径
	public FileSystemXmlApplicationContext(String... configLocations) throws BeansException {
		this(configLocations, true, null);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files and automatically
	 * refreshing the context.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 * @throws BeansException if context creation failed
	 */
	// 这个构造函数在允许configLocation包含多个BeanDefinition的文件路径的同事，还允许执行自己的双亲ioc容器
	public FileSystemXmlApplicationContext(String[] configLocations, ApplicationContext parent) throws BeansException {
		this(configLocations, true, parent);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * @throws BeansException if context creation failed
	 * @see #refresh()
	 */
	public FileSystemXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this(configLocations, refresh, null);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * @param parent the parent context
	 * @throws BeansException if context creation failed
	 * @see #refresh()
	 */
	// 在对象初始化的过程中，调用refresh函数载入BeanDefinition，这个refresh启动了
	// BeanDefinition的载入过程，bean在ioc容器的注入主要分为资源定位，载入 和注册 ，完全实现解耦来实现
	// 关于这个类继承了AbstractXmlApplication，关于ioc容器功能相关的实现，都是在FileSystemXMLApplication中来完成的，
	public FileSystemXmlApplicationContext(
			String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
			throws BeansException {

		super(parent);
		// 实现了对configuration进行处理，
		// 主要目的是让所有配置在文件系统中的，以xml文件方式存在的BeanDefinition都能够得到有效的处理。
		setConfigLocations(configLocations);
		// refresh来启动Ioc容器的初始化，这个方法是非常重要的，这也是分析容器初始化过程实现的一个重要入口
		if (refresh) {
			refresh();
		}
	}


	/**
	 * Resolve resource paths as file system paths.
	 * <p>Note: Even if a given path starts with a slash, it will get
	 * interpreted as relative to the current VM working directory.
	 * This is consistent with the semantics in a Servlet container.
	 * @param path path to the resource
	 * @return the Resource handle
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#getResourceByPath
	 */
	// 这是应用于文件系统汇总的Resource的实现，通过构造一个FileSystemResource来得到一个在文件系统中定位到的BeanDefinition
	// 这个getResourceByPath是在BeanDeifition的loadBeanDefinitions中被调用的
	//loadBeanDefintions采用了模板模式，具体的定位实现实际上由个个子类来完成的

	// 实现getResourceByPath方法，这个方法是一个模板方法，是为读取Resource服务的。

	// 这个方法返回的是一个FileSystemResource对象，通过这个对象，Spring可以进行相关的I/O操作，完成BeanDefinition的定位和解析。
	@Override
	protected Resource getResourceByPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return new FileSystemResource(path);
	}

}
