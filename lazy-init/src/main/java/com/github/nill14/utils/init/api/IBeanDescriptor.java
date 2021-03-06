package com.github.nill14.utils.init.api;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import com.google.common.reflect.TypeToken;

public interface IBeanDescriptor<T> {

	List<? extends IMemberDescriptor> getFieldDescriptors();

	List<? extends IMemberDescriptor> getMethodDescriptors();

	List<? extends IMemberDescriptor> getConstructorDescriptors();

	Set<Class<? super T>> getInterfaces();

	Set<Class<? super T>> getDeclaredTypes();

	TypeToken<T> getToken();
	
	Class<T> getRawType();
	
	Type getGenericType();

	boolean canBeInstantiated();

}