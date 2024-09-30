package fr.ifremer.scoop3.computeParameters;

import java.util.ArrayList;
import java.util.List;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.messages.ComputedParameterMessageItem;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;
import fr.ifremer.scoop3.model.parameter.ParametersRelationships;
import fr.ifremer.scoop3.tools.ComputeParameter;

public class ComputeParametersManager {

    /**
     * Reference on the ComputeParametersManager
     */
    protected static ComputeParametersManager instance = null;

    protected ComputeParametersManager() {
	instance = this;
    }

    /**
     * Try to compute parameters if authorized.
     *
     * @param controller
     */
    public void tryToComputeAllParameters(final AbstractDataBaseManager abstractDataBaseManager, final Report report,
	    final DatasetType datasetType, final List<Observation> observations) {
	final List<Observation> observationsList = new ArrayList<>(observations);
	for (final Observation observation : observationsList) {
	    if (computeDensityAnomalyAuthorized() && (observation.getParameter("DENS") == null)
		    && (observation.getParameter("DENS_ADJUSTED") == null)) {
		tryToComputeDensityAnomaly(abstractDataBaseManager, report, observation, datasetType);
		tryToComputeDensityAdjustedAnomaly(abstractDataBaseManager, report, observation);
	    }
	    if (observation.getParameter("SPEED") == null) {
		tryToComputeSpeed(abstractDataBaseManager, report, observation, datasetType);
	    }

	    // Try to compute if HCSP and HCDT exist and check if EWCT and NSCT do NOT exist OR EWCT and NSCT exist and
	    // check if HCSP and HCDT do NOT exist
	    if (computeCurrentComponentsAuthorized() && (((observation.getParameter("EWCT") == null)
		    && (observation.getParameter("NSCT") == null))
		    || ((observation.getParameter("HCDT") == null) && (observation.getParameter("HCSP") == null)))) {
		tryToComputeCurrentComponents(abstractDataBaseManager, report, observation);
	    }

	}
    }

    /**
     * Force to compute parameters if authorized. Useful after removing measures
     *
     * @param controller
     */
    public void forceToComputeAllParameters(final AbstractDataBaseManager abstractDataBaseManager, final Report report,
	    final DatasetType datasetType, final Observation observation) {
	if (computeDensityAnomalyAuthorized()) {
	    tryToComputeDensityAnomaly(abstractDataBaseManager, report, observation, datasetType);
	    tryToComputeDensityAdjustedAnomaly(abstractDataBaseManager, report, observation);
	}

	tryToComputeSpeed(abstractDataBaseManager, report, observation, datasetType);

	if (computeCurrentComponentsAuthorized()) {
	    tryToComputeCurrentComponents(abstractDataBaseManager, report, observation);
	}
    }

    /**
     * Get the PRES Parameter from an observation (or NULL).
     *
     * @param observation
     * @param datasetType
     * @return NULL if the parameter is not found
     */
    protected Parameter<Double> getPresParameter(final Observation observation, final DatasetType datasetType) {
	if (datasetType == DatasetType.PROFILE) {
	    return observation.getZ();
	}
	return observation.getOceanicParameter(getPresParameterName());
    }

    /**
     * Check if it is possible to compute the Current Components EWCY and NSCT for the given observation.
     *
     * @param abstractDataBaseManager
     * @param observation
     */
    protected void tryToComputeCurrentComponents(final AbstractDataBaseManager abstractDataBaseManager,
	    final Report report, final Observation observation) {
	final OceanicParameter hcspParameter = observation.getOceanicParameter(getHcspParameterName());
	final OceanicParameter hcdtParameter = observation.getOceanicParameter(getHcdtParameterName());
	final OceanicParameter ewctParameter = observation.getOceanicParameter(getEwctParameterName());
	final OceanicParameter nsctParameter = observation.getOceanicParameter(getNsctParameterName());

	// Check if HCSP and HCDT exist and check if EWCT and NSCT do NOT exist
	if ((hcspParameter != null) && (hcdtParameter != null) && (ewctParameter == null) && (nsctParameter == null)) {
	    final double ewctDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager, observation,
		    getEwctParameterName());
	    final double nsctDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager, observation,
		    getNsctParameterName());
	    final OceanicParameter[] currentComponents = ComputeParameter.computeCurrentComponents(hcdtParameter,
		    hcspParameter, ewctDefaultValue, getEwctParameterName(), nsctDefaultValue, getNsctParameterName());
	    try {
		for (final OceanicParameter currentComponent : currentComponents) {
		    observation.addOceanicParameter(currentComponent);

		    final ArrayList<Parameter<? extends Number>> fathers = new ArrayList<>();
		    fathers.add(hcdtParameter);
		    fathers.add(hcspParameter);

		    for (final Parameter<? extends Number> father : fathers) {
			ParametersRelationships.memorizeParametersRelation(currentComponent, father);
		    }

		    final ComputedParameterMessageItem mess = new ComputedParameterMessageItem(observation,
			    currentComponent, fathers);
		    if (report != null) {
			report.addComputedParameterMessage(mess);
		    }
		}
	    } catch (final Exception e) {
		// Should never happen
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}
	// Check if EWCT and NSCT exist and check if HCSP and HCDT do NOT exist
	if ((ewctParameter != null) && (nsctParameter != null) && (hcspParameter == null) && (hcdtParameter == null)) {
	    final double hcdtDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager, observation,
		    getHcdtParameterName());
	    final double hcspDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager, observation,
		    getHcspParameterName());
	    final OceanicParameter[] currentDirAndAmplitude = ComputeParameter.computeCurrentDirectionAndAmplitude(
		    ewctParameter, nsctParameter, hcdtDefaultValue, getHcdtParameterName(), hcspDefaultValue,
		    getHcspParameterName());
	    try {
		for (final OceanicParameter currentVar : currentDirAndAmplitude) {
		    observation.addOceanicParameter(currentVar);

		    final ArrayList<Parameter<? extends Number>> fathers = new ArrayList<>();
		    fathers.add(ewctParameter);
		    fathers.add(nsctParameter);

		    for (final Parameter<? extends Number> father : fathers) {
			ParametersRelationships.memorizeParametersRelation(currentVar, father);
		    }

		    final ComputedParameterMessageItem mess = new ComputedParameterMessageItem(observation, currentVar,
			    fathers);
		    if (report != null) {
			report.addComputedParameterMessage(mess);
		    }
		}
	    } catch (final Exception e) {
		// Should never happen
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}
	// If all parameters are present, set EWCT and NSCT as "LINKED_PARAMETER" (HCDT and HCSP are the fathers)
	if ((hcspParameter != null) && (hcdtParameter != null) && (ewctParameter != null) && (nsctParameter != null)) {
	    ewctParameter.setLinkParamType(LINK_PARAM_TYPE.LINKED_PARAMETER);
	    ParametersRelationships.memorizeParametersRelation(ewctParameter, hcdtParameter);
	    ParametersRelationships.memorizeParametersRelation(ewctParameter, hcspParameter);

	    nsctParameter.setLinkParamType(LINK_PARAM_TYPE.LINKED_PARAMETER);
	    ParametersRelationships.memorizeParametersRelation(nsctParameter, hcdtParameter);
	    ParametersRelationships.memorizeParametersRelation(nsctParameter, hcspParameter);
	}
    }

    /**
     * Check if it is possible to compute the salinity parameter for the given observation.
     *
     * @param abstractDataBaseManager
     * @param observation
     * @param datasetType
     */
    public void tryToComputeSalinity(final AbstractDataBaseManager abstractDataBaseManager, final Report report,
	    final DatasetType datasetType, final List<Observation> observations) {
	if (computeSalinityAuthorized()) {
	    for (final Observation observation : observations) {
		final Parameter<Double> presParameter = getPresParameter(observation, datasetType);
		final OceanicParameter tempParameter = observation.getOceanicParameter(getTempParameterName());
		final OceanicParameter cndcParameter = observation.getOceanicParameter(getCndcParameterName());

		OceanicParameter psalParameter = observation.getOceanicParameter(getPsalParameterName());

		if ((presParameter != null) && (tempParameter != null) && (cndcParameter != null)
			&& (psalParameter == null)) {
		    final double salinityDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager,
			    observation, getPsalParameterName());
		    psalParameter = ComputeParameter.computeSalinity(presParameter, tempParameter, cndcParameter,
			    salinityDefaultValue, getPsalParameterName());
		    try {
			observation.addOceanicParameter(psalParameter);

			final ArrayList<Parameter<? extends Number>> fathers = new ArrayList<>();
			fathers.add(presParameter);
			fathers.add(tempParameter);
			fathers.add(cndcParameter);

			for (final Parameter<? extends Number> father : fathers) {
			    ParametersRelationships.memorizeParametersRelation(psalParameter, father);
			}

			final ComputedParameterMessageItem mess = new ComputedParameterMessageItem(observation,
				psalParameter, fathers);
			if (report != null) {
			    report.addComputedParameterMessage(mess);
			}
		    } catch (final Exception e) {
			// Should never happen
			SC3Logger.LOGGER.error(e.getMessage(), e);
		    }
		}
	    }
	}
    }

    /**
     * Check if it is possible to compute the Density parameter for the given observation.
     *
     * @param abstractDataBaseManager
     * @param observation
     * @param datasetType
     */
    protected void tryToComputeDensityAnomaly(final AbstractDataBaseManager abstractDataBaseManager,
	    final Report report, final Observation observation, final DatasetType datasetType) {
	final Parameter<Double> presParameter = getPresParameter(observation, datasetType);
	final OceanicParameter tempParameter = observation.getOceanicParameter(getTempParameterName());
	final OceanicParameter psalParameter = observation.getOceanicParameter(getPsalParameterName());

	OceanicParameter densAnoParameter;

	if ((presParameter != null) && (tempParameter != null) && (psalParameter != null)) {
	    final double densityAnomalyDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager,
		    observation, getDensityAnomalyParameterName());
	    densAnoParameter = ComputeParameter.computeDensityAnomaly(presParameter, tempParameter, psalParameter,
		    densityAnomalyDefaultValue, getDensityAnomalyParameterName());
	    try {
		observation.addOceanicParameter(densAnoParameter);

		final ArrayList<Parameter<? extends Number>> fathers = new ArrayList<>();
		fathers.add(presParameter);
		fathers.add(tempParameter);
		fathers.add(psalParameter);

		for (final Parameter<? extends Number> father : fathers) {
		    ParametersRelationships.memorizeParametersRelation(densAnoParameter, father);
		}

		final ComputedParameterMessageItem mess = new ComputedParameterMessageItem(observation,
			densAnoParameter, fathers);
		if (report != null) {
		    report.addComputedParameterMessage(mess);
		}
	    } catch (final Exception e) {
		// Should never happen
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Check if it is possible to compute the Density adjusted parameter for the given observation.
     *
     * @param abstractDataBaseManager
     * @param observation
     * @param datasetType
     */
    protected void tryToComputeDensityAdjustedAnomaly(final AbstractDataBaseManager abstractDataBaseManager,
	    final Report report, final Observation observation) {

	final Parameter<Double> presParameter = observation.getOceanicParameter("PRES_ADJUSTED");
	final OceanicParameter tempParameter = observation.getOceanicParameter("TEMP_ADJUSTED");
	final OceanicParameter psalParameter = observation.getOceanicParameter("PSAL_ADJUSTED");

	OceanicParameter densAnoParameter;

	if ((presParameter != null) && (tempParameter != null) && (psalParameter != null)) {
	    final double densityAnomalyDefaultValue = getFillValueForTheComputedParameter(abstractDataBaseManager,
		    observation, getDensityAnomalyAdjustedParameterName());
	    densAnoParameter = ComputeParameter.computeDensityAnomaly(presParameter, tempParameter, psalParameter,
		    densityAnomalyDefaultValue, getDensityAnomalyAdjustedParameterName());
	    if (densAnoParameter.getCode() == null) {
		densAnoParameter.setCode("DENS_ADJUSTED");
	    }
	    try {
		observation.addOceanicParameter(densAnoParameter);
		final ArrayList<Parameter<? extends Number>> fathers = new ArrayList<>();
		fathers.add(presParameter);
		fathers.add(tempParameter);
		fathers.add(psalParameter);

		for (final Parameter<? extends Number> father : fathers) {
		    ParametersRelationships.memorizeParametersRelation(densAnoParameter, father);
		}

		final ComputedParameterMessageItem mess = new ComputedParameterMessageItem(observation,
			densAnoParameter, fathers);
		if (report != null) {
		    report.addComputedParameterMessage(mess);
		}
	    } catch (final Exception e) {
		// Should never happen
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}
    }

    public void tryToComputeSpeed(final AbstractDataBaseManager abstractDataBaseManager, final Report report,
	    final Observation observation, final DatasetType datasetType) {
	// empty method
    }

    /**
     * By default, the Current Components are not computed. Override if needed.
     *
     * @return
     */
    protected boolean computeCurrentComponentsAuthorized() {
	return false;
    }

    /**
     * By default, the Density Anomaly is not computed. Override if needed.
     *
     * @return
     */
    protected boolean computeDensityAnomalyAuthorized() {
	return false;
    }

    /**
     * By default, the Conductivity is not computed. Override if needed.
     *
     * @return
     */
    protected boolean computeSalinityAuthorized() {
	return false;
    }

    /**
     * @return the Density Anomaly Parameter Name
     */
    protected String getDensityAnomalyParameterName() {
	return null;
    }

    /**
     * @return the EWCT Parameter Name
     */
    protected String getEwctParameterName() {
	return null;
    }

    /**
     * Get the fillValue for the given computed parameter
     *
     * @param abstractDataBaseManager
     * @param observation
     * @param parameterName
     */
    protected double getFillValueForTheComputedParameter(final AbstractDataBaseManager abstractDataBaseManager,
	    final Observation observation, final String parameterName) {
	return 0;
    }

    /**
     * @return the HCDT Parameter Name
     */
    protected String getHcdtParameterName() {
	return null;
    }

    /**
     * @return the HCSP Parameter Name
     */
    protected String getHcspParameterName() {
	return null;
    }

    /**
     * @return the NSCT Parameter Name
     */
    protected String getNsctParameterName() {
	return null;
    }

    /**
     * @return the Pressure Parameter Name
     */
    protected String getPresParameterName() {
	return null;
    }

    /**
     * @return the Salinity Parameter Name
     */
    protected String getPsalParameterName() {
	return null;
    }

    /**
     * @return the Conductivity Parameter Name
     */
    protected String getCndcParameterName() {
	return null;
    }

    /**
     * @return the Temperature Parameter Name
     */
    protected String getTempParameterName() {
	return null;
    }

    /**
     * @return the Speed Parameter Name
     */
    protected String getSpeedParameterName() {
	return null;
    }

    protected String getDensityAnomalyAdjustedParameterName() {
	return null;
    }
}
