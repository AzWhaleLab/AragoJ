package com.aragoj.session;

import com.aragoj.session.model.*;
import com.aragoj.equation.model.EquationItem;

import javax.xml.bind.*;
import java.io.File;
import java.io.FileNotFoundException;

public class SessionUtils {


    public static boolean saveSession(Session session){
        if(session == null || session.getPath() == null) return false;
        File file = new File(session.getPath());
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Session.class, EditorItem.class, EditorItemLine.class, EquationItem.class, EditorItemZoom.class, EditorItemArea.class, EditorItemPosition.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(session, file);
            return true;
        } catch (JAXBException e) {
            System.err.println(e);
            return false;
        }
    }

    public static Session openSession(File file) throws FileNotFoundException {
        if(file == null){
            throw new FileNotFoundException();
        }
        if(file.exists()){
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Session.class, EditorItem.class, EditorItemLine.class, EquationItem.class, EditorItemZoom.class, EditorItemArea.class, EditorItemPosition.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Session session = (Session) jaxbUnmarshaller.unmarshal(file);
                session.setPath(file.getPath());
                return session;
            } catch (JAXBException e) {
                e.printStackTrace();
                System.err.println(e);
            }
        }
        throw new FileNotFoundException();
    }
}
