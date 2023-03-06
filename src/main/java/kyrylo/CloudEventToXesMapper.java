package kyrylo;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.xeslite.lite.factory.XAttributeMapLiteImpl;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kyrylo.VariableType.getTypeForId;

public class CloudEventToXesMapper {
    private static final XFactoryLiteImpl factory = new XFactoryLiteImpl();

    public static XLog createLogFromEvents(List<EventDto> pageOfEvents) {
        final XLog log = createEmptyLog();

        final Map<String, List<EventDto>> traceIdToTrace = pageOfEvents.stream().collect(Collectors.groupingBy(EventDto::getTraceId));
        traceIdToTrace.forEach((traceId, events) -> {
            XTrace trace = createTrace(traceId);

            events.forEach(e -> trace.add(mapToXEvent(e)));
            log.add(trace);
        });
        return log;
    }

    private static XTrace createTrace(final String traceId) {
        XAttributeMapLiteImpl traceAttributes = new XAttributeMapLiteImpl();
        traceAttributes.put("concept:name", new XAttributeLiteralImpl("concept:name", traceId));
        return factory.createTrace(traceAttributes);
    }

    public static XLog createEmptyLog() {
        return factory.createLog();
    }

    public static XEvent mapToXEvent(EventDto e) {
        String eventName = e.getEventName();
        if (eventName.contains("$")) {
            eventName = eventName.split("\\$")[0];
        }
        Object data = e.getData();
        String group = e.getGroup();
        String id = e.getId();
        String source = e.getSource();
        Long ingestionTimestamp = e.getIngestionTimestamp();
        Long timestamp = e.getTimestamp();

        XAttributeMapLiteImpl eventAttributes = new XAttributeMapLiteImpl();
        eventAttributes.putAll(extractSimpleVariables(data));
        eventAttributes.put("concept:name", new XAttributeLiteralImpl("concept:name", eventName));
        eventAttributes.put("group", new XAttributeLiteralImpl("group", group));
        eventAttributes.put("source", new XAttributeLiteralImpl("source", source));
        eventAttributes.put("ingestionTimestamp", new XAttributeTimestampImpl("ingestionTimestamp", ingestionTimestamp));
        eventAttributes.put("time:timestamp", new XAttributeTimestampImpl("time:timestamp", timestamp));
        return new XEventImpl(XID.parse(id), eventAttributes);
    }

    public static XAttributeMapLiteImpl extractSimpleVariables(Object variable) {
        final XAttributeMapLiteImpl result = new XAttributeMapLiteImpl();
        if (variable != null) {
            if (variable instanceof Map) {
                final Map<String, Object> data = (Map<String, Object>) variable;
                data.entrySet().stream()
                        .filter(stringObjectEntry -> stringObjectEntry.getValue() != null)
                        .forEach(stringObjectEntry -> {
                            String key = stringObjectEntry.getKey();
                            String value = extractVariableValue(stringObjectEntry.getValue());
                            VariableType variableType = getTypeForId(extractVariableType(stringObjectEntry.getValue()));

                            switch (variableType) {
                                case STRING:
                                case OBJECT:
                                case JSON:
                                    result.put(key, new XAttributeLiteralImpl(key, value));
                                    break;
                                case BOOLEAN: result.put(key, new XAttributeBooleanImpl(key, Boolean.parseBoolean(value)));
                                    break;
                                case SHORT:
                                case LONG:
                                case INTEGER:
                                    result.put(key, new XAttributeDiscreteImpl(key, Long.parseLong(value)));
                                    break;
                                case DATE: result.put(key, new XAttributeTimestampImpl(key, Long.parseLong(value)));
                                    break;
                                case DOUBLE: result.put(key, new XAttributeContinuousImpl(key, Double.parseDouble(value)));
                                    break;
                            }
                        });
            }
        }
        return result;
    }

    private static String extractVariableType(final Object variableValue) {
        if (variableValue instanceof Collection) {
            List<Object> valueList = (List<Object>) variableValue;
            return valueList.isEmpty()
                    ? VariableType.STRING.getId()
                    : getVariableTypeFromValuesList((List<Object>) variableValue);
        } else {
            return variableValue.getClass().getSimpleName();
        }
    }

    private static String getVariableTypeFromValuesList(final List<Object> variableValues) {
        if (variableValues.get(0) instanceof Number) {
            return VariableType.DOUBLE.getId();
        } else {
            return variableValues.get(0).getClass().getSimpleName();
        }
    }

    private static String extractVariableValue(final Object variableValue) {
        if (variableValue instanceof Collection) {
            return ((Collection<?>) variableValue).stream().map(Object::toString).collect(Collectors.joining(","));
        } else {
            return variableValue.toString();
        }
    }
}
