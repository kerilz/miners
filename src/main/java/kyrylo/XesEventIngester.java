package kyrylo;

import com.raffaeleconforti.log.util.LogImporter;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.camunda.discovery.EventDto;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.camunda.discovery.util.LogUtil.readSingleLog;

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
        List<org.camunda.discovery.EventDto> events = new ArrayList<>();
        traces.forEach((key, value) -> value.forEach(e -> {
//            if (Objects.equals(getLifecycleAttribute(e), "complete")) {
                events.add(xEventToEventDto(e, key, logName));
//            }
        }));
        ingestEvents(events);
    }

    public static org.camunda.discovery.EventDto xEventToEventDto(XEvent event, String traceId, String logName) {
        org.camunda.discovery.EventDto eventDto = new org.camunda.discovery.EventDto();
        eventDto.setEventName(getStringAttributeValue(event, "concept:name") + getLifecycleAttribute(event));
        eventDto.setTimestamp(((XAttributeTimestamp)event.getAttributes().get("time:timestamp")).getValueMillis());
        eventDto.setSource("external");
        eventDto.setId(UUID.randomUUID().toString());
        eventDto.setGroup(logName);
        eventDto.setIngestionTimestamp(new Date().getTime());
        eventDto.setTraceId(traceId);
        return eventDto;
    }

    private static String getLifecycleAttribute(XEvent event) {
        String lifecycleStateValue = ((XAttributeLiteral) event.getAttributes().get("lifecycle:transition")).getValue();
        switch (lifecycleStateValue) {
            case "start": lifecycleStateValue = "_start";
                break;
            case "complete": lifecycleStateValue = "_end";
                break;
            default: break;
        };
        return lifecycleStateValue;
    }

    private static String getStringAttributeValue(XEvent event, String key) {
        return ((XAttributeLiteral) event.getAttributes().get(key)).getValue();
    }

    public static void ingestEvents(List<EventDto> events) {
        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8090/api/ingestion/event/batch/mapped")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer SASAT")
                .body(events).asJson();
        System.out.println(response.getBody().toString());

    }
}
