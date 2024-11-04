/*
 * Copyright (C) 2024 Prasanta Paul, https://github.com/paul-prasanta
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
package com.pras.cache;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Gson (com.google.code.gson, v2.11.0) based JSON serializer for Redis Cache.
 * <p>
 * Default Redis Jackson serializer stores Class name mapping in generated JSON. This may be redundant when we know POJO object structure.
 * <p>
 * This serializer is lite and uses Type definition during deserialization. It doesn't require Class name embedding, thus compress generated JSON and preserve Cache space. Also takes care UTC timezone and ISO Date Format.
 * <p>
 * <b>Usage:</b> 
 * <p>
 * Use separate Serializer instance for each type of POJO or Model object. Define Cache wise configuration using RedisCacheManagerBuilderCustomizer
 * <p>
 * <code>
 * RedisCacheConfiguration.defaultCacheConfig()
 * .serializeValuesWith(SerializationPair.fromSerializer(new RedisGsonSerializer(RedisGsonSerializer.asList(Post.class))) // List of Object serialization
 * </code>
 * <p>
 * <code>
 * RedisCacheConfiguration.defaultCacheConfig()
 * .serializeValuesWith(SerializationPair.fromSerializer(new RedisGsonSerializer(Post.class))) // Object serialization
 * </code>
 * @see <a href="https://github.com/google/gson/issues/281">Gson UTC Date Issue</a>
 * 
 * @author Prasanta Paul
 */
public class RedisGsonSerializer implements RedisSerializer {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	final Type type;
	final Gson gson;
	
	public RedisGsonSerializer(Type type) {
		logger.info("<< GSON Init >>...type: {}", type);
		this.type = type;
		/*
		 * Limitations of default Gson
		 * ---------------------------
		 * 1. new Gson(); // Serialize dates in non ISO format e.g. Oct 28, 2024, 12:53:17 PM
		 * 2. new GsonBuilder().setDateFormat(ISO_DATE_FROMAT) // Export date in ISO, but internally handle dates in Local timezone instead of UTC
		 */
		this.gson = new GsonBuilder()
				.registerTypeAdapter(Date.class, new UtcIsoDateAdapter())
				.create();
	}
	
	@Override
	public byte[] serialize(Object value) throws SerializationException {
		logger.info("<< GSON Serializaion >>...{}", value);
		return gson.toJson(value).getBytes();
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		try {
			String s = new String(bytes, "UTF-8");
			logger.info("<< GSON DeSerializaion >>...{} / {}", type, s);
			return gson.fromJson(s, this.type);
			
		} catch (JsonSyntaxException e) {
			throw new SerializationException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(e.getMessage());
		}
	}
	
	public static Type asList(Class clazz) {
		return TypeToken.getParameterized(List.class, clazz).getType();
	}
	
	private class UtcIsoDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
		// ISO Date format with milliseconds (SSS e.g. 000), timezone offsets (XXX e.g. +05:30)
		final String ISO_DATE_FROMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
		final SimpleDateFormat dateFormat;
		
		public UtcIsoDateAdapter() {
			dateFormat = new SimpleDateFormat(ISO_DATE_FROMAT, Locale.getDefault());
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		
		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			String ds = dateFormat.format(src);
			logger.info("< Serialize Date > {} / {}", src, ds);
			return new JsonPrimitive(ds);
		}
		
		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				String ds = json.getAsString();
				logger.info("< Deserialize Date > {}", ds);
				return dateFormat.parse(json.getAsString());
				
			} catch (ParseException e) {
				throw new JsonParseException(e);
			}
		}
	}
}
