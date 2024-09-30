package fr.ifremer.scoop3.gui.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ifremer.scoop3.chart.model.ChartDataset;
import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.gui.common.CommonViewModel;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.io.IGetNearestProfiles;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;

public class DataViewModel extends CommonViewModel {

    private final AbstractDataBaseManager abstractDataBaseManager;
    private final Set<String> allParametersCode = new LinkedHashSet<>();
    protected ChartDataset chartDataset;
    private Map<String, List<String>> parametersOrder;

    private boolean isParameterListFiltered; // if the parameter have to be filtered
    private Map<String, String> parameterListFiltered; // The parameters to be used. Index = code, value = name.

    public DataViewModel(final Dataset dataset, final Report report, final Map<String, List<String>> parametersOrder,
	    final AbstractDataBaseManager abstractDataBaseManager) {
	super(dataset, report);

	this.parametersOrder = parametersOrder;
	this.abstractDataBaseManager = abstractDataBaseManager;

	isParameterListFiltered = false;

	fillAllParameterscode();
	convertScoop3ModelToChartModel();
    }

    /**
     * Convert the scoop3 model to chart model
     */
    public synchronized void convertScoop3ModelToChartModel() {

	chartDataset = new ChartDataset();

	// For each observation
	final Set<ChartPhysicalVariable> refChartPhysicalVariables = new HashSet<>();
	// final List<String> referenceList = new ArrayList<String>();
	final List<Platform> tempPlatformList = new ArrayList<>(getDataset().getPlatforms());

	// Construct the set of reference code
	final Set<String> referenceCodeList = new HashSet<>();
	for (final Platform platform : tempPlatformList) {
	    final List<Observation> tempObservationList = new ArrayList<>(platform.getAllObservations());
	    for (final Observation obs : tempObservationList) {
		final Parameter<? extends Number> referenceParameter = obs.getReferenceParameter();
		referenceCodeList.add(referenceParameter.getCode());

	    }
	}
	// Construct ChartDatset
	for (final Platform platform : tempPlatformList) {
	    final List<Observation> tempObservationList = new ArrayList<>(platform.getAllObservations());
	    for (final Observation obs : tempObservationList) {
		// Stock ZParameter values into the unique ChartPhysicalVariable
		// for this parameter
		final Parameter<? extends Number> referenceParameter = obs.getReferenceParameter();

		ChartPhysicalVariable refPhysicalVariable;
		int index = chartDataset.indexOfPhysicalVariable(referenceParameter.getCode());
		if (index == -1) {
		    refPhysicalVariable = new ChartPhysicalVariable(referenceParameter.getCode(),
			    referenceParameter.getUnit());
		    refPhysicalVariable.setReferenceParameter(true);
		    refPhysicalVariable.setIsADate(referenceParameter instanceof TimeParameter);
		    chartDataset.addPhysicalVariable(refPhysicalVariable);
		    // Add referece code to list to avoid problem are DEPH and PRES in the same dataset
		    // referenceList.add(referenceParameter.getCode());

		    // If there are DEPH and PRES in the same dataset :o/
		    // Add empty data like an offset if other ref already exist
		    if (!refChartPhysicalVariables.isEmpty()) {
			final ChartPhysicalVariable refChartVariable = refChartPhysicalVariables.iterator().next();
			for (final String ptfCode : refChartVariable.getPlatformsCodes()) {
			    refPhysicalVariable.addValuesAndQCs(ptfCode, platform.getAllObservations(), new Double[0],
				    new char[0]);
			}
		    }

		} else {
		    refPhysicalVariable = chartDataset.getPhysicalVariable(index);
		    chartDataset.setPhysicalVariable(index, refPhysicalVariable);
		}
		refPhysicalVariable.addValuesAndQCs(platform.getCode(), platform.getAllObservations(),
			Conversions.convertNumberListToDoubleArray(referenceParameter.getValues()),
			QCValues.convertQCValuesListToCharArray(referenceParameter.getQcValues()));

		refChartPhysicalVariables.add(refPhysicalVariable);

		// If there are DEPH and PRES in the same dataset :o/
		// Add empty data in other ref
		for (final ChartPhysicalVariable refChartPhysicalVariable : refChartPhysicalVariables) {
		    if (refChartPhysicalVariable != refPhysicalVariable) {
			refChartPhysicalVariable.addValuesAndQCs(platform.getCode(), platform.getAllObservations(),
				new Double[0], new char[0]);
		    }
		}

		// Stock level values into the unique ChartPhysicalVariable for
		// this parameter
		ChartPhysicalVariable levelPhysicalVariable;
		index = chartDataset.indexOfPhysicalVariable(CommonViewModel.MEASURE_NUMBER);
		if (index == -1) {
		    levelPhysicalVariable = new ChartPhysicalVariable(CommonViewModel.MEASURE_NUMBER, "");
		    levelPhysicalVariable.setLevelParameter(true);
		    chartDataset.addPhysicalVariable(levelPhysicalVariable);
		} else {
		    levelPhysicalVariable = chartDataset.getPhysicalVariable(index);
		    chartDataset.setPhysicalVariable(index, levelPhysicalVariable);
		}
		final int measureCount = referenceParameter.getValues().size();
		final Double[] measureNumbers = new Double[measureCount];
		for (int i = 0; i < measureCount; i++) {
		    measureNumbers[i] = (double) (i + 1);
		}
		levelPhysicalVariable.addValuesAndQCs(platform.getCode(), platform.getAllObservations(), measureNumbers,
			QCValues.convertQCValuesListToCharArray(referenceParameter.getQcValues()));

		// System.err.println(" For each oceanic parameter");
		// For each oceanic parameter
		if (parametersOrder == null) {
		    for (final OceanicParameter op : obs.getOceanicParameters().values()) {
			// System.out.println(" getCode() : " + op.getCode() + " - Size : " + op.getValues().size());
			addParameter(platform, op);
		    }
		} else {
		    if (parametersOrder.get(obs.getReference()) != null) {
			for (final String parameterName : parametersOrder.get(obs.getReference())) {
			    final OceanicParameter param = obs.getOceanicParameter(parameterName);
			    // param == null if it is the ReferenceParameter and final ReferenceParameter is final not
			    // Adjusted
			    if ((param != null) && !referenceCodeList.contains(param.getCode())) {
				// System.out.println(obs.getId() + " -> getCode() : " + param.getCode() + " - Size : "
				// + param.getValues().size());
				addParameter(platform, param);
			    }
			}
		    }
		}

		// Check if there are multiple Latitude values
		if ((obs.getLatitude().getValues().size() > 1) || (dataset.getDatasetType() == DatasetType.TIMESERIE)) {
		    addParameter(platform, obs.getLatitude());
		}
		// Check if there are multiple Longitude values
		if ((obs.getLongitude().getValues().size() > 1)
			|| (dataset.getDatasetType() == DatasetType.TIMESERIE)) {
		    addParameter(platform, obs.getLongitude());
		}
		// Check if there are multiple Z values
		if ((obs.getReferenceParameter() != obs.getZ()) && (obs.getZ() != null)
			&& (obs.getZ().getValues().size() > 1)) {
		    addParameter(platform, obs.getZ());
		}
		// Check if there are multiple Time values
		if ((obs.getReferenceParameter() != obs.getTime()) && (obs.getTime().getValues().size() > 1)) {
		    addParameter(platform, obs.getTime());
		}

		for (final String allParameter : allParametersCode) {

		    if (parametersOrder.get(obs.getReference()) != null) {

			boolean addParam = false;
			if (referenceCodeList.contains(allParameter)) {
			    // Dont add param if it's a ref param
			    addParam = false;
			} else {
			    if (((parametersOrder != null)
				    && !parametersOrder.get(obs.getReference()).contains(allParameter))
				    || ((parametersOrder == null) && (obs.getOceanicParameter(allParameter) == null))) {
				addParam = true;
			    }
			}

			if (addParam) {
			    // This observation does NOT have this parameter.
			    // To avoid problems, we add an empty Oceanic Parameter
			    addParameter(platform, getEmptyParameter(allParameter));
			}
		    }
		}
	    }
	}
    }

    public void fillAllParameterscode() {
	if (parametersOrder != null) {
	    for (final List<String> parameters : parametersOrder.values()) {
		for (final String parameter : parameters) {
		    // Check each observations ...
		    for (final Observation obs : dataset.getObservations()) {
			for (final OceanicParameter op : obs.getOceanicParameters().values()) {
			    if (parameter.equals(op.getCode())) {
				allParametersCode.add(parameter);
			    }
			    if (allParametersCode.contains(parameter)) {
				break;
			    }
			}
			if (allParametersCode.contains("TEMP_ADJUSTED") && allParametersCode.contains("PSAL_ADJUSTED")
				&& (allParametersCode.contains("PRES_ADJUSTED")
					|| allParametersCode.contains("DEPH_ADJUSTED"))) {
			    allParametersCode.add("DENS_ADJUSTED");
			}
			if (allParametersCode.contains(parameter)) {
			    break;
			}
		    }
		}
	    }
	} else {
	    // Check each observations ...
	    for (final Observation obs : dataset.getObservations()) {
		// Add each Oceanic Parameter
		for (final OceanicParameter op : obs.getOceanicParameters().values()) {
		    allParametersCode.add(op.getCode());
		}
	    }
	}
    }

    /**
     * @return the abstractDataBaseManager
     */
    public AbstractDataBaseManager getAbstractDataBaseManager() {
	return abstractDataBaseManager;
    }

    /**
     * @return the chartDataset
     */
    public ChartDataset getChartDataset() {
	return chartDataset;
    }

    /**
     * @param observationNumber
     * @return
     */
    public List<Profile> getNearestProfilesForAllPlatforms(final int observationNumber) {
	if ((abstractDataBaseManager instanceof IGetNearestProfiles)
		&& (getDataset().getDatasetType() == DatasetType.PROFILE)) {
	    try {
		return ((IGetNearestProfiles) abstractDataBaseManager).getNearestProfilesForAllPlatforms(getReport(),
			getPlatformForObservation(observationNumber), (Profile) getObservation(observationNumber));
	    } catch (final SQLException e) {
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}
	return null;
    }

    /**
     * @param observationNumber
     * @return
     */
    public List<Profile> getNearestProfilesForCurrentPlatform(final int observationNumber) {
	if ((abstractDataBaseManager instanceof IGetNearestProfiles)
		&& (getDataset().getDatasetType() == DatasetType.PROFILE)) {
	    try {
		return ((IGetNearestProfiles) abstractDataBaseManager).getNearestProfilesForCurrentPlatform(getReport(),
			getPlatformForObservation(observationNumber), (Profile) getObservation(observationNumber));
	    } catch (final SQLException e) {
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}
	return null;
    }

    /**
     * @return the parametersOrder
     */
    public Map<String, List<String>> getParametersOrder() {
	return parametersOrder;
    }

    public void setParametersOrder(final Map<String, List<String>> parametersOrder) {
	this.parametersOrder = parametersOrder;
    }

    /**
     * Add a "Double" Parameter to the chartDataset
     *
     * @param op
     */
    protected void addParameter(final Platform platform, final Parameter<? extends Number> op) {
	int index;
	ChartPhysicalVariable physicalVariable;
	index = chartDataset.indexOfPhysicalVariable(op.getCode());
	if (index == -1) {
	    physicalVariable = new ChartPhysicalVariable(op.getCode(), op.getUnit());
	    chartDataset.addPhysicalVariable(physicalVariable);
	} else {
	    physicalVariable = chartDataset.getPhysicalVariable(index);
	    chartDataset.setPhysicalVariable(index, physicalVariable);
	}

	physicalVariable.addValuesAndQCs(platform.getCode(), platform.getAllObservations(),
		Conversions.convertNumberListToDoubleArray(op.getValues()),
		QCValues.convertQCValuesListToCharArray(op.getQcValues()));
    }

    /**
     * @param allParameter
     * @return
     */
    protected OceanicParameter getEmptyParameter(final String allParameter) {
	return new OceanicParameter(allParameter);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewModel#getStepType()
     */
    @Override
    protected STEP_TYPE getStepType() {
	return STEP_TYPE.Q2_CONTROL_AUTO_DATA;
    }

    @Override
    protected void specificPrepareForDispose() {
	chartDataset.prepareForDispose();
    }

    public Integer getValidatedObservationsNumber() {
	return null;
    }

    public boolean isParameterListFiltered() {
	return isParameterListFiltered;
    }

    public void setParameterListFiltered(final boolean isParameterListFiltered) {
	this.isParameterListFiltered = isParameterListFiltered;
    }

    public Map<String, String> getParameterListFiltered() {
	return parameterListFiltered;
    }

    public void setParameterListFiltered(final Map<String, String> parameterListFiltered) {
	this.parameterListFiltered = parameterListFiltered;
    }
}
