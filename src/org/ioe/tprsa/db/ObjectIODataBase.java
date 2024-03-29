package org.ioe.tprsa.db;

import java.io.File;

import org.ioe.tprsa.classify.speech.CodeBookDictionary;
import org.ioe.tprsa.classify.speech.HMMModel;

public class ObjectIODataBase implements DataBase {
    /**
     * type of current model,,gmm,hmm,cbk, which is extension ofsaved file
     */
    String type;
    String[] modelFiles;
    String CURRENTFOLDER;
    /**
     * the file name to same codebook, adds .cbk extension automatically
     */
    String CODEBOOKFILENAME = "codebook";

    /**
     * MAKE SURE THAT Files are/will be in this folder structure
     * the folder structure for training :
     * (Selected)DBROOTFOLDER\
     * \speechTrainWav\\apple\\apple01.wav
     * \speechTrainWav\\apple\\apple02.wav
     * \speechTestWav\\cat\\cat01.wav
     * \speechTestWav\\cat\\cat01.wav
     * \speechTestWav\\cat\\cat01.wav
     * \codeBook\\codeBook.cbk
     * \models\\HMM\\apple.hmm
     * \models\\HMM\\cat.hmm
     * \models\\GMM\\ram.gmm
     * \models\\GMM\\shyam.gmm
     */
    public ObjectIODataBase() {}

    /**
     * @param type type of the model, valid entry are either gmm, hmm, or cbk
     */
    public void setType(String type) {
        this.type = type;
        if (this.type.equalsIgnoreCase("hmm")) {
            CURRENTFOLDER = "models\\HMM";
        }
        if (this.type.equalsIgnoreCase("cbk")) {
            CURRENTFOLDER = "models\\codeBook";
        }
    }

    @Override
    public Model readModel(String name) {
        Model model = null;
        if (type.equalsIgnoreCase("hmm")) {
            ObjectIO<HMMModel> oio = new ObjectIO<>();
            model = oio.readModel( CURRENTFOLDER + "\\" + name + "." + type);
        }
        if (type.equalsIgnoreCase("cbk")) {
            ObjectIO<CodeBookDictionary> oio = new ObjectIO<>();
            model = oio.readModel( CURRENTFOLDER + "\\" + CODEBOOKFILENAME + "." + type);
        }
        return model;
    }

    @Override
    public String[] readRegistered() {
        modelFiles = readRegisteredWithExtension();
        System.out.println("modelFiles length (Oiodb) :" + modelFiles.length);
        return removeExtension(modelFiles);
    }

    @Override
    public void saveModel(Model model, String name) {
        if (type.equalsIgnoreCase("hmm")) {
            ObjectIO<HMMModel> oio = new ObjectIO<>();
            oio.setModel((HMMModel) model);
            oio.saveModel(CURRENTFOLDER + "\\" + name
                    + "." + type);
        }
        if (type.equalsIgnoreCase("cbk")) {
            ObjectIO<CodeBookDictionary> oio = new ObjectIO<>();
            oio.setModel((CodeBookDictionary) model);
            oio.saveModel( CURRENTFOLDER + "\\"
                    + CODEBOOKFILENAME + "." + type);
        }
    }

    private String[] readRegisteredWithExtension() {
        File modelPath = new File( CURRENTFOLDER);
        modelFiles = new String[modelPath.list().length];
        modelFiles = modelPath.list();// must return only folders
        return modelFiles;
    }

    private String[] removeExtension(String[] modelFiles) {
        // remove the ext i.e., type
        String[] noExtension = new String[modelFiles.length];
        for (int i = 0; i < modelFiles.length; i++) {
            noExtension[i] = modelFiles[i].substring(0,
                    modelFiles[i].length() - 4);// TODO:check the lengths
        }
        return noExtension;
    }
}