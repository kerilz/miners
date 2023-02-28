import BpmnModeler from 'bpmn-js/lib/Modeler';
const diagram = process.argv[2]


const modeler = new BpmnModeler({
    container: '#js-canvas',
    keyboard: {
        bindTo: window
    }
});

modeler
    .importXML(diagram)
    .then(({ warnings }) => {
        if (warnings.length) {
            console.log(warnings);
        }

        const canvas = modeler.get("canvas");

        canvas.zoom("fit-viewport");

        const elementRegistry = modeler.get("elementRegistry");
        const modeling = modeler.get("modeling");

        elementRegistry
            .getAll()
            .filter((el) => el.waypoints)
            .forEach((connection) => {
                modeling.reconnectStart(
                    connection,
                    connection.source,
                    connection.waypoints[0]
                );
                modeling.reconnectEnd(
                    connection,
                    connection.target,
                    connection.waypoints[connection.waypoints.length - 1]
                );
            });

        canvas.zoom("fit-viewport");

        return modeler.saveXML({ format: true });
    })
    .then((xml) => console.log(xml.xml))
    .catch((err) => {
        console.log(err);
    });