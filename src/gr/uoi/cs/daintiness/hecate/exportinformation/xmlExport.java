package gr.uoi.cs.daintiness.hecate.exportinformation;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import gr.uoi.cs.daintiness.hecate.transitions.Deletion;
import gr.uoi.cs.daintiness.hecate.transitions.Insersion;
import gr.uoi.cs.daintiness.hecate.transitions.TransitionList;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;
import gr.uoi.cs.daintiness.hecate.transitions.Update;

public class xmlExport {
	public  void export(Transitions transition, String path) {
		try {
			getDirectory getPath = new getDirectory();
			JAXBContext jaxbContext = JAXBContext.newInstance(Update.class,
					Deletion.class, Insersion.class,
					TransitionList.class, Transitions.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			String filePath = getPath.getDir(path) + File.separator + "transitions.xml";
			jaxbMarshaller.marshal(transition, new FileOutputStream(filePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
