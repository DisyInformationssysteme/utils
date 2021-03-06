package com.github.nill14.utils.init.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Provider;

import com.github.nill14.utils.init.api.ILazyPojo;
import com.github.nill14.utils.init.api.IPojoFactory;
import com.github.nill14.utils.init.api.IPojoInitializer;
import com.github.nill14.utils.init.api.IPropertyResolver;
import com.google.common.reflect.TypeToken;

@SuppressWarnings("serial")
public final class LazyPojo<T> implements ILazyPojo<T> {

	
	public static <T> ILazyPojo<T> forSingleton(T singleton) {
		return forSingleton(singleton, IPropertyResolver.empty());
	}
	
	public static <T> ILazyPojo<T> forSingleton(T singleton, IPropertyResolver resolver) {
		IPojoFactory<T> pojoFactory = PojoProviderFactory.singleton(singleton, resolver);
		return new LazyPojo<T>(pojoFactory, IPojoInitializer.empty());
	}
	
	public static <T> ILazyPojo<T> forBean(Class<T> beanClass) {
		return forBean(beanClass, IPropertyResolver.empty(), IPojoInitializer.empty());
	}
	
	public static <T> ILazyPojo<T> forBean(Class<T> beanClass, IPropertyResolver resolver, IPojoInitializer initializer) {
		IPojoFactory<T> factory = new PojoInjectionFactory<>(TypeToken.of(beanClass), resolver);
		return new LazyPojo<>(factory, initializer);
	}
	
	public static <T, F extends Provider<? extends T>> ILazyPojo<T> forProvider(Class<F> providerClass) {
		return forProvider(providerClass, IPropertyResolver.empty(), IPojoInitializer.empty());
	}
	
	public static <T, F extends Provider<? extends T>> ILazyPojo<T> forProvider(
			Class<F> providerClass, IPropertyResolver resolver, IPojoInitializer factoryInitializer) {
		
		TypeToken<F> providerType = TypeToken.of(providerClass);
		PojoFactoryAdapter<T, F> factoryAdapter = new PojoFactoryAdapter<T, F>(providerType, resolver, factoryInitializer);
		return new LazyPojo<>(factoryAdapter, factoryAdapter);
	}
	
	public static <T> ILazyPojo<T> forProvider(
			Provider<T> provider, IPropertyResolver resolver, IPojoInitializer initializer) {
		IPojoFactory<T> pojoFactory = new PojoProviderFactory<>(provider, resolver);
		return new LazyPojo<>(pojoFactory, initializer);
	}
	
	public static <T> ILazyPojo<T> forFactory(
			IPojoFactory<T> pojoFactory, IPojoInitializer initializer) {
		return new LazyPojo<>(pojoFactory, initializer);
	}
	
	private final IPojoFactory<T> factory;
	private final IPojoInitializer initializer;
	private volatile transient T instance;

	public LazyPojo(IPojoFactory<T> factory, IPojoInitializer initializer) {
		this.factory = factory;
		this.initializer = initializer;
	}
	
	@Override
	public TypeToken<T> getType() {
		return factory.getType();
	}

	@Override
	public T getInstance() {
		T instance = this.instance;
		if (instance == null) {
			
			synchronized (this) {
				instance = this.instance;
				if (instance == null) {
					
					instance = factory.newInstance();
					initializer.init(this, factory, instance);
					this.instance = instance;
				}
			}
		}
		return instance;
	}

	@Override
	public boolean freeInstance() {
		boolean released = false;
		T instance = this.instance;
		if (instance != null) {
			
			synchronized (this) {
				instance = this.instance;
				if (instance != null) {
					
					this.instance = null;
					initializer.destroy(this, factory, instance);
					released = true;
				}
			}
		}
		
		return released;
	}

	@Override
	public Future<T> init(ExecutorService executor) {
		return executor.submit(new Callable<T>() {

			@Override
			public T call() throws Exception {
				return getInstance();
			}
		});
	}

	@Override
	public Future<Boolean> destroy(ExecutorService executor) {
		return executor.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return freeInstance();
			}
		});
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		synchronized (this) {
			if (instance instanceof Serializable) {
				stream.writeObject(instance);
			} else {
				stream.writeObject(null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		Object obj = stream.readObject();
		synchronized (this) {
			if (obj != null) {
				this.instance = (T) obj;
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)@%s", 
				getClass().getSimpleName(), 
				factory.getType().toString(), 
				Integer.toHexString(System.identityHashCode(this)));
	}
	
	@Override
	public Provider<T> toProvider() {
		return provider;
	}

	
	private transient Provider<T> provider = new Provider<T>() {

		@Override
		public T get() {
			return getInstance();
		}
	};

}
