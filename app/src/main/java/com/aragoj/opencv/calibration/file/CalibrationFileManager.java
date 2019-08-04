package com.aragoj.opencv.calibration.file;

import com.aragoj.opencv.calibration.model.CalibrationModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;

public class CalibrationFileManager {

    public static boolean saveCalibration(File file, CalibrationModel model){
        if(file == null || model == null) return false;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CalibrationData.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(new CalibrationData(model), file);
            return true;
        } catch (JAXBException e) {
            System.err.println("Could not save " + file.getName() + ","+ "e=" + e.getCause().getMessage());
            return false;
        }
    }

    public static CalibrationModel getCalibration(File file) throws FileNotFoundException {
        if(file == null){
            throw new FileNotFoundException();
        }
        if(file.exists()){
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(CalibrationData.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                CalibrationData session = (CalibrationData) jaxbUnmarshaller.unmarshal(file);
                CalibrationModel model = new CalibrationModel(session);
                return model;
            } catch (JAXBException e) {
                e.printStackTrace();
//                System.err.println("Could not save " + file.getName() + ","+ "e=" + e.getCause().getMessage());
            }
        }
        throw new FileNotFoundException();
    }
}
