package kyrylo;

import com.raffaeleconforti.log.util.LogImporter;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class XesEventIngester {
    public static void ingestLog(String path) throws Exception {
        System.out.println("Injection initialized...");
        String[] fileName = path.split("\\/");
        String logName = fileName[fileName.length - 1].split("\\.")[0];
        XLog log = LogImporter.importFromFile(new XFactoryLiteImpl(), path);
        ingestLog(log, logName);
    }
    public static void ingestLog(XLog log, String logName) {
        System.out.println("Ingesting log [ " + logName + "]");
        Map<String, XTrace> traces = new HashMap<>();
        log.forEach(trace -> {
            String traceId = ((XAttributeLiteral) trace.getAttributes().get("concept:name")).getValue();
            traces.put(traceId, trace);
        });
        List<EventDto> events = new ArrayList<>();
        traces.forEach((key, value) -> value.forEach(e -> {
//            if (Objects.equals(getLifecycleAttribute(e), "complete")) {
                events.add(xEventToEventDto(e, key, logName));
//            }
        }));
        ingestEventsInChunks(events);
    }

    public static EventDto xEventToEventDto(XEvent event, String traceId, String logName) {
        EventDto eventDto = new EventDto();
        eventDto.setEventName(getStringAttributeValue(event, "concept:name") + getLifecycleAttribute(event));
        eventDto.setTimestamp(((XAttributeTimestamp)event.getAttributes().get("time:timestamp")).getValueMillis());
        eventDto.setSource(logName);
        eventDto.setId(UUID.randomUUID().toString());
        eventDto.setGroup("external");
        eventDto.setIngestionTimestamp(new Date().getTime());
        eventDto.setTraceId(traceId);
        return eventDto;
    }

    private static String getLifecycleAttribute(XEvent event) {
        String lifecycleStateValue = ((XAttributeLiteral) event.getAttributes().get("lifecycle:transition")).getValue();
        switch (lifecycleStateValue) {
            case "start": lifecycleStateValue = "$start";
                break;
            case "complete": lifecycleStateValue = "$end";
                break;
            default: break;
        };
        return lifecycleStateValue;
    }

    private static String getStringAttributeValue(XEvent event, String key) {
        return ((XAttributeLiteral) event.getAttributes().get(key)).getValue();
    }

    public static void ingestEvents(List<EventDto> events) {
//        final List<CloudEventRequestDto> collect = events.stream()
//          .map(CloudEventRequestDto::map)
//          .collect(Collectors.toList());
        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8090/api/ingestion/event/batch/mapped")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer VERY_SECURE_TOKEN")
                .body(events).asJson();
        System.out.println(response.getStatus());
    }

    public static void ingestEventsInChunks(List<EventDto> events) {
        int chunkSize = 500;
        List<EventDto> chunk = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            chunk.add(events.get(i));
            if (chunk.size() == chunkSize || i == events.size() - 1) {
                ingestEvents(chunk);
                chunk.clear();
            }
        }
    }

    public static class CloudEventRequestDto {
        private String id;
        private String source;
        private String specversion;
        private String type;
        private String time;
        private Object data;
        private String traceid;
        private String group;

        public CloudEventRequestDto() {
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setSource(final String source) {
            this.source = source;
        }

        public void setSpecversion(final String specversion) {
            this.specversion = specversion;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public void setTime(final String time) {
            this.time = time;
        }

        public void setData(final Object data) {
            this.data = data;
        }

        public void setTraceid(final String traceid) {
            this.traceid = traceid;
        }

        public void setGroup(final String group) {
            this.group = group;
        }

        public static CloudEventRequestDto map(EventDto event) {
            CloudEventRequestDto cloudEvent = new CloudEventRequestDto();
            cloudEvent.setId(event.getId());
            cloudEvent.setSource(event.getSource());
            cloudEvent.setSpecversion("1.0");
            cloudEvent.setType(event.getEventName());
            cloudEvent.setTime(convert(event.getTimestamp()));
            cloudEvent.setData(event.getData());
            cloudEvent.setTraceid(event.getTraceId());
            cloudEvent.setGroup(event.getGroup());
            return cloudEvent;
        }
        public static String convert(long epochTimestamp) {
            Date date = new Date(epochTimestamp);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.format(date);
        }
    }
}
