package kyrylo;

import au.edu.qut.processmining.log.LogParser;
import au.edu.qut.processmining.log.SimpleLog;
import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult;
import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XElement;
import org.deckfour.xes.model.XLog;
import org.processmining.fodina.Fodina;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.bpmn.BpmnDefinitions;
import org.processmining.plugins.bpmnminer.miner.BPMNMiner;
import org.processmining.plugins.bpmnminer.types.MinerSettings;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MiningRestService {
  public static final String prepend = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><definitions xmlns=\"http://www.omg" +
    ".org/spec/BPMN/20100524/MODEL\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:bpmndi=\"http://www.omg" +
    ".org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:xsi=\"http://www.w3" +
    ".org/2001/XMLSchema-instance\" targetNamespace=\"http://www.omg.org/bpmn20\" exporter=\"ProM. http://www.promtools" +
    ".org/prom6\" exporterVersion=\"6.3\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">";

  public static final String append = "\n</definitions>";

  @PostMapping("/fodina")
  public String mineFodina(@RequestBody List<EventDto> events) {
    final XLog xLog = CloudEventToXesMapper.createLogFromEvents(events);
    return mineFodina(xLog, getLogSize(events));
  }

  @PostMapping("/split")
  public String mineSplit(@RequestBody List<EventDto> events) {
    final XLog xLog = CloudEventToXesMapper.createLogFromEvents(events);
    return mineSplit(xLog);
  }

  @PostMapping("/bpmn_miner")
  public String mineBPMN(@RequestBody List<EventDto> events) {
    final XLog xLog = CloudEventToXesMapper.createLogFromEvents(events);
    return mineBPMN(xLog, getLogSize(events));
  }

  @RequestMapping(
    value = "/test",
    method = RequestMethod.GET,
    produces="text/plain"
  )
  @ResponseBody
  public String mineTest() throws Exception {
    final XLog xLog = LogImporter.importFromFile(new XFactoryLiteImpl(), "/Users/kyrylo/Downloads/data/activitylog_uci_detailed_weekends.xes");
    return mineBPMN(xLog, getLogSize(xLog));
  }

  public static String mineFodina(XLog xLog, final int logSize) {
    Fodina fodina = new Fodina();
    final SimpleLog simpleLog = LogParser.getSimpleLog(xLog, new XEventNameClassifier());
    final BPMNDiagram diagram = fodina.discoverBPMNDiagram(simpleLog, new MinerSettings(logSize));

    return retrieveContent(new FakePluginContext(), diagram);
  }

  public static String mineSplit(XLog xLog) {
    double epsilon = 0.1;
    double eta = 0.4;
    boolean replaceIORs = true;

    SplitMiner yam = new SplitMiner();
    final BPMNDiagram diagram1 = yam.mineBPMNModel(
      xLog,
      new XEventNameClassifier(),
      eta,
      epsilon,
      DFGPUIResult.FilterType.FWG,
      true,
      replaceIORs,
      true,
      SplitMinerUIResult.StructuringTime.NONE
    );


    return retrieveContent(new FakePluginContext(), diagram1);
  }

  public static String mineBPMN(XLog xLog, final int logSize) {
    BPMNMiner miner = new BPMNMiner(xLog, new MinerSettings(logSize));
    final BPMNDiagram diagram = miner.mine();
    return retrieveContent(new FakePluginContext(), diagram);
  }

  private static String retrieveContent(PluginContext context, BPMNDiagram diagram) {
    BpmnDefinitions.BpmnDefinitionsBuilder definitionsBuilder = new BpmnDefinitions.BpmnDefinitionsBuilder(context, diagram);
    BpmnDefinitions definitions = new BpmnDefinitions("definitions", definitionsBuilder);
    return prepend + definitions.exportElements() + append;
  }

  private static int getLogSize(List<EventDto> events) {
    List<String> existingTraceIds = new ArrayList<>();
    events.forEach(e -> {
      if (!existingTraceIds.contains(e.getTraceId())) {
        existingTraceIds.add(e.getTraceId());
      }
    });
    return existingTraceIds.size();
  }
  private static int getLogSize(XLog xLog) {
    return xLog.size();
  }

  private static String getElementId(XElement element) {
    return ((XAttributeLiteral) element.getAttributes().get("concept:name")).getValue();
  }
}
