package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.*;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.fasterxml.jackson.databind.jsonschema.types.Schema;
import com.fasterxml.jackson.databind.jsonschema.types.SchemaType;
import com.fasterxml.jackson.databind.jsonschema.visitors.JsonFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.visitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Generic handler for types that implement {@link JsonSerializable}.
 *<p>
 * Note: given that this is used for anything that implements
 * interface, can not be checked for direct class equivalence.
 */
@JacksonStdImpl
public class SerializableSerializer
    extends StdSerializer<JsonSerializable>
{
    public final static SerializableSerializer instance = new SerializableSerializer();

    // Ugh. Should NOT need this...
    private final static AtomicReference<ObjectMapper> _mapperReference = new AtomicReference<ObjectMapper>();
    
    protected SerializableSerializer() { super(JsonSerializable.class); }

    @Override
    public void serialize(JsonSerializable value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException
    {
        value.serialize(jgen, provider);
    }

    @Override
    public final void serializeWithType(JsonSerializable value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        value.serializeWithType(jgen, provider, typeSer);
    }
    
    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitor visitor, JavaType typeHint)
    {
        if (typeHint == null) {
        	visitor.anyFormat();
        } else  {
            Class<?> rawClass = typeHint.getRawClass();
            if (rawClass.isAnnotationPresent(JsonSerializableSchema.class)) {
                JsonSerializableSchema schemaInfo = rawClass.getAnnotation(JsonSerializableSchema.class);
                
                if (!JsonSerializableSchema.NO_VALUE.equals(schemaInfo.schemaObjectPropertiesDefinition())) {
                	visitor.objectFormat(typeHint);
                    //objectProperties = schemaInfo.schemaObjectPropertiesDefinition();
                } else 
                if (!JsonSerializableSchema.NO_VALUE.equals(schemaInfo.schemaItemDefinition())) {
                    visitor.arrayFormat(typeHint);
                	//itemDefinition = schemaInfo.schemaItemDefinition();
                } else {
                	visitor.anyFormat();
                	//visitor.forFormat(SchemaType.valueOf(schemaInfo.schemaType()));
                }
            } else {
            	visitor.anyFormat();
            }
        } 
        /* 19-Mar-2012, tatu: geez, this is butt-ugly abonimation of code...
         *    really, really should not require back ref to an ObjectMapper.
         */
//        objectNode.put("type", schemaType);
//        if (objectProperties != null) {
//            try {
//                objectNode.put("properties", _getObjectMapper().readTree(objectProperties));
//            } catch (IOException e) {
//                throw new JsonMappingException("Failed to parse @JsonSerializableSchema.schemaObjectPropertiesDefinition value");
//            }
//        }
//        if (itemDefinition != null) {
//            try {
//                objectNode.put("items", _getObjectMapper().readTree(itemDefinition));
//            } catch (IOException e) {
//                throw new JsonMappingException("Failed to parse @JsonSerializableSchema.schemaItemDefinition value");
//            }
//        }

    }
    
    private final static synchronized ObjectMapper _getObjectMapper()
    {
        ObjectMapper mapper = _mapperReference.get();
        if (mapper == null) {
            mapper = new ObjectMapper();
            _mapperReference.set(mapper);
        }
        return mapper;
    }
}
