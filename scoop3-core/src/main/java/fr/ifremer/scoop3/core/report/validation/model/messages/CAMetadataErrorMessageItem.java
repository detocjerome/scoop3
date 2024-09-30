package fr.ifremer.scoop3.core.report.validation.model.messages;

import java.util.List;

import org.jdom2.Element;

import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.model.QCValues;

public class CAMetadataErrorMessageItem extends CAErrorMessageItem {

    /**
     *
     */
    private static final long serialVersionUID = -6712583587826751100L;

    /**
     * To know which QC to update
     */
    public enum QC_TO_UPDATE {
	DATASET_END_DATE, // Means it is a Metadata in the Dataset to update
	DATASET_METADATA, // The code of the first Platform
	DATASET_PTF_CODE, // The label of the first Platform
	DATASET_PTF_LABEL, // Dataset End date ...
	DATASET_REFERENCE, // Dataset reference ...
	DATASET_START_DATE, // Dataset Start date ...
	NOTHING_TO_UPDATE, // Means that the error message is not linked to a Metadata to update
	OBS_METADATA, // Means it is a Metadata in the Obs1 to update
	OBS_OCEAN_DEPTH, // QC to update is the attribute OceanDepth of the Obs1
	SENSOR_DIST_FROM_BOTTOM, // QC to update is the attribute "NominalDepth" of the Sensor linked to the Obs1
	SENSOR_NOMINAL_DEPTH, //
	SENSOR_SAMPLING_RATE, //
    }

    public static final String XML_ELEMENT_CA_OBS_METADATA_ERROR_MESSAGE = "caObsMetadataErrorMessageItem";
    private static final String XML_ELEMENT_IS_GENERATE_DURING_LAST_CONTROL = "isGenerateDuringLastControl";
    private static final String XML_ELEMENT_METADATA = "metadataName";
    private static final String XML_ELEMENT_QC_TO_UPDATE = "qcToUpdate";

    private boolean isGenerateDuringLastControl = true;
    private final String metadata;
    private QC_TO_UPDATE qcToUpdate;

    private static QC_TO_UPDATE convertQcToUpdateFromMetadataValueChange(
	    final MetadataValueChange.QC_TO_UPDATE qcToUpdate) {
	switch (qcToUpdate) {
	case DATASET_END_DATE:
	    return QC_TO_UPDATE.DATASET_END_DATE;
	case DATASET_METADATA:
	    return QC_TO_UPDATE.DATASET_METADATA;
	case DATASET_PTF_CODE:
	    return QC_TO_UPDATE.DATASET_PTF_CODE;
	case DATASET_PTF_LABEL:
	    return QC_TO_UPDATE.DATASET_PTF_LABEL;
	case DATASET_REFERENCE:
	    return QC_TO_UPDATE.DATASET_REFERENCE;
	case DATASET_START_DATE:
	    return QC_TO_UPDATE.DATASET_START_DATE;
	case OBS_METADATA:
	    return QC_TO_UPDATE.OBS_METADATA;
	case OBS_OCEAN_DEPTH:
	    return QC_TO_UPDATE.OBS_OCEAN_DEPTH;
	case OBS_SENSOR_NOMINAL_DEPTH:
	    return QC_TO_UPDATE.SENSOR_NOMINAL_DEPTH;
	case OBS_SENSOR_DIST_FROM_BOTTOM:
	    return QC_TO_UPDATE.SENSOR_DIST_FROM_BOTTOM;
	case OBS_SENSOR_SAMPLING_RATE:
	    return QC_TO_UPDATE.SENSOR_SAMPLING_RATE;
	}
	// Should never happen ...
	return null;
    }

    private static ERROR_MESSAGE_TYPE getErrorMsgType(final QC_TO_UPDATE qcToUpdate) {
	switch (qcToUpdate) {
	case DATASET_END_DATE:
	case DATASET_METADATA:
	case DATASET_PTF_CODE:
	case DATASET_PTF_LABEL:
	case DATASET_REFERENCE:
	case DATASET_START_DATE:
	    return ERROR_MESSAGE_TYPE.DATASET_METADATA;
	case NOTHING_TO_UPDATE:
	case OBS_METADATA:
	case OBS_OCEAN_DEPTH:
	case SENSOR_DIST_FROM_BOTTOM:
	case SENSOR_NOMINAL_DEPTH:
	case SENSOR_SAMPLING_RATE:
	    return ERROR_MESSAGE_TYPE.OBSERVATION_METADATA;
	}
	return null;
    }

    public CAMetadataErrorMessageItem(final Element messageItemElt) {
	super(messageItemElt);

	try {
	    qcToUpdate = QC_TO_UPDATE.valueOf(messageItemElt.getChildText(XML_ELEMENT_QC_TO_UPDATE));
	} catch (final IllegalArgumentException iae) {
	    // This should never happened
	    qcToUpdate = null;
	}
	metadata = messageItemElt.getChildText(XML_ELEMENT_METADATA);

	// By default, set TRUE
	isGenerateDuringLastControl = !"false"
		.equals(messageItemElt.getChildText(XML_ELEMENT_IS_GENERATE_DURING_LAST_CONTROL));
    }

    public CAMetadataErrorMessageItem(final MetadataValueChange.QC_TO_UPDATE qcToUpdate, final String metadata,
	    final String obs1Id, final String details, final QCValues flagAuto) {
	this(convertQcToUpdateFromMetadataValueChange(qcToUpdate), metadata, obs1Id, details, flagAuto);
    }

    public CAMetadataErrorMessageItem(final QC_TO_UPDATE qcToUpdate, final String obs1Id, final String details,
	    final QCValues flagAuto) {
	this(qcToUpdate, null, obs1Id, details, flagAuto);
    }

    public CAMetadataErrorMessageItem(final QC_TO_UPDATE qcToUpdate, final String metadata, final String obs1Id,
	    final String details, final QCValues flagAuto) {
	this(qcToUpdate, metadata, obs1Id, null, details, flagAuto);
    }

    public CAMetadataErrorMessageItem(final QC_TO_UPDATE qcToUpdate, final String metadata, final String obs1Id,
	    final String obs2Id, final String details, final QCValues flagAuto) {
	super(getErrorMsgType(qcToUpdate), obs1Id, obs2Id, details, flagAuto);

	this.qcToUpdate = qcToUpdate;
	this.metadata = metadata;
    }

    public CAMetadataErrorMessageItem(final String metadata, final String obs1Id, final String details,
	    final QCValues flagAuto) {
	this(QC_TO_UPDATE.OBS_METADATA, metadata, obs1Id, details, flagAuto);
    }

    /**
     * @return the metadata
     */
    public String getMetadata() {
	return metadata;
    }

    /**
     * @return the qcToUpdate
     */
    public QC_TO_UPDATE getQcToUpdate() {
	return qcToUpdate;
    }

    public MetadataValueChange.QC_TO_UPDATE getQcToUpdateForMetadataValueChange() {
	switch (qcToUpdate) {
	case DATASET_END_DATE:
	    return MetadataValueChange.QC_TO_UPDATE.DATASET_END_DATE;
	case DATASET_METADATA:
	    return MetadataValueChange.QC_TO_UPDATE.DATASET_METADATA;
	case DATASET_PTF_CODE:
	    return MetadataValueChange.QC_TO_UPDATE.DATASET_PTF_CODE;
	case DATASET_PTF_LABEL:
	    return MetadataValueChange.QC_TO_UPDATE.DATASET_PTF_LABEL;
	case DATASET_REFERENCE:
	    return MetadataValueChange.QC_TO_UPDATE.DATASET_REFERENCE;
	case DATASET_START_DATE:
	    return MetadataValueChange.QC_TO_UPDATE.DATASET_START_DATE;
	case NOTHING_TO_UPDATE:
	    return null;
	case OBS_OCEAN_DEPTH:
	    return MetadataValueChange.QC_TO_UPDATE.OBS_OCEAN_DEPTH;
	case OBS_METADATA:
	    return MetadataValueChange.QC_TO_UPDATE.OBS_METADATA;
	case SENSOR_DIST_FROM_BOTTOM:
	    return MetadataValueChange.QC_TO_UPDATE.OBS_SENSOR_DIST_FROM_BOTTOM;
	case SENSOR_NOMINAL_DEPTH:
	    return MetadataValueChange.QC_TO_UPDATE.OBS_SENSOR_NOMINAL_DEPTH;
	case SENSOR_SAMPLING_RATE:
	    return MetadataValueChange.QC_TO_UPDATE.OBS_SENSOR_SAMPLING_RATE;
	}
	// Should never happen ...
	return null;
    }

    /**
     * @return the isGenerateDuringLastControl
     */
    public boolean isGenerateDuringLastControl() {
	return isGenerateDuringLastControl;
    }

    /**
     * @param isGenerateDuringLastControl
     *            the isGenerateDuringLastControl to set
     */
    public void setIsGenerateDuringLastControl(final boolean isGenerateDuringLastControl) {
	this.isGenerateDuringLastControl = isGenerateDuringLastControl;
    }

    /**
     * @param obs2Id
     *            the obs2Id to set
     */
    public void setObs2Id(final String obs2Id) {
	this.obs2Id = obs2Id;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementAdditionnalInfo()
     */
    @Override
    protected List<Element> getElementAdditionnalInfo() {
	final List<Element> additionalInfo = super.getElementAdditionnalInfo();

	final Element qcToUpdateElt = new Element(XML_ELEMENT_QC_TO_UPDATE);
	qcToUpdateElt.addContent(String.valueOf(qcToUpdate));
	additionalInfo.add(qcToUpdateElt);

	if (metadata != null) {
	    final Element metadataElt = new Element(XML_ELEMENT_METADATA);
	    metadataElt.addContent(metadata);
	    additionalInfo.add(metadataElt);
	}

	final Element isGenerateDuringLastControlElt = new Element(XML_ELEMENT_IS_GENERATE_DURING_LAST_CONTROL);
	isGenerateDuringLastControlElt.addContent(String.valueOf(isGenerateDuringLastControl));
	additionalInfo.add(isGenerateDuringLastControlElt);

	return additionalInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.core.report.validation.model.MessageItem# getElementMessageTitle()
     */
    @Override
    protected String getElementMessageTitle() {
	return XML_ELEMENT_CA_OBS_METADATA_ERROR_MESSAGE;
    }
}
