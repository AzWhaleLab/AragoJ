package com.aragoj.session;

import com.aragoj.equation.model.EquationItem;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EquationFileManager {

    public static final String FILE_PATH = "data/equations.xml";

    public static void exportEquationsToXml(List<EquationItem> equationItems){
        File file = new File(FILE_PATH);

        // Set variables to null since we only want the expression + name
        for (EquationItem item : equationItems){
            item.setVariables(null);
        }

        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EquationItem[].class);
            JAXBElement<EquationItem[]> root = new JAXBElement<EquationItem[]>(new QName("equations"),
                    EquationItem[].class, equationItems.toArray(new EquationItem[equationItems.size()]));
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(root, file);
        } catch (JAXBException e) {
            System.err.println("Could not save equations.xml, e=" + e.getCause().getMessage());
        }
    }

    public static List<EquationItem> importEquationsXml(){
        File file = new File(FILE_PATH);
        if(file.exists()){
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(EquationItem[].class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                InputStream in = new FileInputStream(FILE_PATH);
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLStreamReader parser = factory.createXMLStreamReader(in);

                JAXBElement<EquationItem[]> root = jaxbUnmarshaller.unmarshal(parser, EquationItem[].class);
                return Arrays.asList(root.getValue());
            } catch (JAXBException e) {
//                System.err.println("Could not load equations.xml, e=" + e.toString());
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Could not load equations.xml, file does not exist");
        }
        return new ArrayList<>();
    }
}
