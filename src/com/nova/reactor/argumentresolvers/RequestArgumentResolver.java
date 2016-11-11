package com.nova.reactor.argumentresolvers;

import javafx.util.Pair;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nova.reactor.util.UntilException.rethrowConsumer;
import static java.util.Arrays.stream;

public class RequestArgumentResolver implements HandlerMethodArgumentResolver
{
	private static final Pair<Class<?>, Function<String, Object>>[] PRIMITIVE_PARSE_FUNCTIONS = Stream.of(
			new Pair<Class<?>, Function<String, Object>>(Integer.class, Integer::parseInt),
			new Pair<Class<?>, Function<String, Object>>(Long.class, Long::parseLong),
			new Pair<Class<?>, Function<String, Object>>(Double.class, Double::parseDouble),
			new Pair<Class<?>, Function<String, Object>>(Float.class, Float::parseFloat),
			new Pair<Class<?>, Function<String, Object>>(Boolean.class, Boolean::parseBoolean),
			new Pair<Class<?>, Function<String, Object>>(Byte.class, Byte::parseByte),
			new Pair<Class<?>, Function<String, Object>>(Short.class, Short::parseShort),
			new Pair<Class<?>, Function<String, Object>>(Character.class, x -> x.charAt(0)),
			new Pair<Class<?>, Function<String, Object>>(int.class, Integer::parseInt),
			new Pair<Class<?>, Function<String, Object>>(long.class, Long::parseLong),
			new Pair<Class<?>, Function<String, Object>>(double.class, Double::parseDouble),
			new Pair<Class<?>, Function<String, Object>>(float.class, Float::parseFloat),
			new Pair<Class<?>, Function<String, Object>>(boolean.class, Boolean::parseBoolean),
			new Pair<Class<?>, Function<String, Object>>(byte.class, Byte::parseByte),
			new Pair<Class<?>, Function<String, Object>>(short.class, Short::parseShort),
			new Pair<Class<?>, Function<String, Object>>(char.class, x -> x.charAt(0)))
			.toArray(Pair[]::new);
	
	private static final HashMap<Class<?>, Field[]> FIELD_ASSOCIATIONS = new HashMap<>();
	
	@Override
	public boolean supportsParameter(MethodParameter methodParameter)
	{
		return Object.class.isAssignableFrom(methodParameter.getParameterType());
	}
	
	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception
	{
		return injectVariables(methodParameter.getParameterType(), nativeWebRequest);
	}
	
	private static Object injectVariables(Class<?> type, NativeWebRequest request) throws InstantiationException, IllegalAccessException
	{
		return injectVariables(type, request, "");
	}
	
	private static Object injectVariables(Class<?> type, NativeWebRequest request, String prefix) throws IllegalAccessException, InstantiationException
	{
		Object obj = type.newInstance();
		
		if (!FIELD_ASSOCIATIONS.containsKey(type))
		{
			addFieldAssociation(type);
		}
		
		stream(FIELD_ASSOCIATIONS.get(type)).forEach(rethrowConsumer(field -> setFieldValueFromRequest(field, request, prefix, obj)));
		
		return obj;
	}
	
	private static void setFieldValueFromRequest(Field field, NativeWebRequest request, String prefix, Object obj) throws InstantiationException, IllegalAccessException
	{
		String paramValue = request.getParameter(prefix + field.getName());
		
		Object value = null;
		
		Optional<Function<String, Object>> primitiveFunction = stream(PRIMITIVE_PARSE_FUNCTIONS).filter(x -> x.getKey() == field.getType()).map(Pair::getValue).findFirst();
		
		if (primitiveFunction.isPresent())
		{
			if (paramValue != null)
			{
				value = primitiveFunction.get().apply(paramValue);
			}
		}
		else if (field.getType() == String.class)
		{
			value = paramValue;
		}
		else
		{
			value = injectVariables(field.getType(), request, prefix + field.getName() + ".");
		}
		
		if (value != null)
		{
			setFieldValue(field, obj, value);
		}
	}
	
	private static void setFieldValue(Field field, Object obj, Object value) throws IllegalAccessException
	{
		field.setAccessible(true);
		
		field.set(obj, value);
		
		field.setAccessible(false);
	}
	
	private static void addFieldAssociation(Class<?> type)
	{
		ArrayList<Field> params = new ArrayList<>();
		
		Class<?> current = type;
		
		while (current.getSuperclass() != null)//stream(type.getInterfaces()).anyMatch(x -> x == AuthenticatedRequestTemplate.class))
		{
			Field[] fields = current.getDeclaredFields();
			
			stream(fields).filter(x -> !Modifier.isStatic(x.getModifiers()) && Modifier.isPrivate(x.getModifiers())).forEach(x -> params.add(x));
			
			current = current.getSuperclass();
		}
		
		ArrayList<String> invalid = stream(type.getMethods()).filter(x -> x.getName().startsWith("get") && params.stream().allMatch(y -> !camelCase(x.getName().substring(3)).equals(y.getName())))
				.map(x -> camelCase(x.getName().substring(3)))
				.collect(Collectors.toCollection(ArrayList::new));
		
		FIELD_ASSOCIATIONS.put(type, params.stream().filter(field -> !invalid.contains(field.getName())).toArray(Field[]::new));
	}
	
	private static String camelCase(String str)
	{
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}
}