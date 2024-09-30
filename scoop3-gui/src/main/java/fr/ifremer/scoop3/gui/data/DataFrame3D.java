/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ifremer.scoop3.gui.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.gui.jzy3dManager.Jzy3dManager.DataFrameType;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

/**
 * Cette classe met en mémoire un dataset scoop sous forme de table (analogue à un data.frame de R ou d'un résultat de
 * select en base de donnée) :
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0">
 * <tr>
 * <th>lon</th>
 * <th>lat</th>
 * <th>date</th>
 * <th>depth</th>
 * <th>TEMP</th>
 * <th>FLU2</th>
 * <th>DOX2</th>
 * <th>PSAL</th>
 * </tr>
 * <tr>
 * <td>0.004833</td>
 * <td>2.006167</td>
 * <td>1.370173e+12</td>
 * <td>4</td>
 * <td>28.445</td>
 * <td>20.5902</td>
 * <td>Double.NaN</td>
 * <td>34.174</td>
 * </tr>
 * <tr>
 * <td>0.004833</td>
 * <td>2.006167</td>
 * <td>1.370173e+12</td>
 * <td>5</td>
 * <td>28.470</td>
 * <td>20.5739</td>
 * <td>Double.NaN</td>
 * <td>34.172</td>
 * </tr>
 * <tr>
 * <td>0.004833</td>
 * <td>2.006167</td>
 * <td>1.370173e+12</td>
 * <td>6</td>
 * <td>28.454</td>
 * <td>20.6149</td>
 * <td>Double.NaN</td>
 * <td>34.173</td>
 * </tr>
 * </table>
 * Pour chaque observation autant de lignes que d'immersions définies pour cette observation.<br />
 * Tous les paramètres présent dans le dataset sont présents sur la ligne, les valeurs des parametres absents pour
 * l'observation sont égales à Double.NaN. Cette mise en mémoire est basée sur une HashMap.
 *
 * @author Michel LAROUR
 */
public class DataFrame3D {
    /**
     * la HashMap du dataframe, le KeySet correspond aux colonnes du dataframe
     */
    private final HashMap<String, ArrayList<Double>> dataframe = new HashMap<String, ArrayList<Double>>();
    /** le KeySet des paramètres ("TEMP","PSAL", etc ... */
    private final ArrayList<String> parameterKeySet = new ArrayList<String>();

    /** longitude key (Key en plus des paramètres) */
    public static final String LONGITUDE_KEY = "LON";

    /** latitude key (Key en plus des paramètres) */
    public static final String LATITUDE_KEY = "LAT";

    /** date key (Key en plus des paramètres) */
    public static final String DATE_KEY = "DATE";

    /**
     * time key (Key en plus des paramètres) utile seulement pour les graphs timeSeries
     */
    public static final String TIME_KEY = "TIME";

    /** immersion key (Key en plus des paramètres) */
    public static final String DEPTH_KEY = "DEPH";

    public static final String DEPTH_ADJUSTED_KEY = "DEPH_ADJUSTED";

    public static final String PRES_KEY = "PRES";

    public static final String PRES_ADJUSTED_KEY = "PRES_ADJUSTED";

    public static final String CYCLE_NUMBER = "CYCLE NUMBER";

    private int totalSize = 0;

    private boolean useCycleNumber;

    /**
     * Constructeur du dataframe à partir du modèle de données scoop
     *
     * @param aDataset
     *            le modèle de données scoop
     */
    public DataFrame3D(final Dataset aDataset, final String xJComboBox, final String yJComboBox,
	    final String zJComboBox, final String colorMapJComboBox, final DataFrameType pythonType,
	    final SuperposedModeEnum superposedModeEnum, final String currentObservationIndex) {
	if (DataFrameType.NORMAL.equals(pythonType)) {
	    loadDataSet(aDataset, xJComboBox, yJComboBox, zJComboBox, colorMapJComboBox, superposedModeEnum,
		    currentObservationIndex);
	}
	if (DataFrameType.FIRST_ONLY.equals(pythonType)) {
	    loadTempDataSet(aDataset, xJComboBox, yJComboBox, zJComboBox, colorMapJComboBox);
	}
    }

    /**
     * Change les données du dataframe courant à partir du modèle de données scoop
     *
     * @param dataset
     *            le modèle de données scoop
     */
    private void loadDataSet(final Dataset dataset, final String xJComboBox, final String yJComboBox,
	    final String zJComboBox, final String colorMapJComboBox, final SuperposedModeEnum superposedModeEnum,
	    final String currentObservationIndex) {
	try {
	    if (parameterKeySet.size() <= 4) {
		if (dataframe.get(xJComboBox) == null) {
		    dataframe.put(xJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(xJComboBox);
		if (dataframe.get(yJComboBox) == null) {
		    dataframe.put(yJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(yJComboBox);
		if (dataframe.get(zJComboBox) == null) {
		    dataframe.put(zJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(zJComboBox);
		if (dataframe.get(colorMapJComboBox) == null) {
		    dataframe.put(colorMapJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(colorMapJComboBox);
	    }

	    // si le dataset.type == TIMESERIE, on a besoin de time et des oceanicParameters
	    if (dataset.getDatasetType() == DatasetType.TIMESERIE) {
		totalSize = 0;
		// if current platform mode, display all observations of the platform
		if (superposedModeEnum == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET) {
		    for (final Observation observation : dataset.getPlatforms()
			    .get(Integer.parseInt(currentObservationIndex) - 1).getAllObservations()) {
			buildTimeSerieDataset(observation);
		    }
		}
		// if all observations mode, display all observations of the dataset
		else if (superposedModeEnum == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET) {
		    for (final Observation observation : dataset.getObservations()) {
			buildTimeSerieDataset(observation);
		    }
		}
		// if current obs only
		else {
		    buildTimeSerieDataset(dataset.getObservations().get(Integer.parseInt(currentObservationIndex) - 1));
		}
	    }

	    // si le dataset.type == PROFILE, on a besoin de depth, lon, lat, date et des oceanicParameters
	    if (dataset.getDatasetType() == DatasetType.PROFILE) {
		totalSize = 0;
		// if current platform mode, display all observations of the platform
		if (superposedModeEnum == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET) {
		    // determine si tous les cycle numbers sont égaux ou pas (dans ce cas on utilise
		    // l'observationCounter)
		    useCycleNumber = checkCycleNumber(dataset.getPlatforms()
			    .get(Integer.parseInt(currentObservationIndex) - 1).getAllObservations());
		    int observationCounter = 0;
		    for (final Observation observation : dataset.getPlatforms()
			    .get(Integer.parseInt(currentObservationIndex) - 1).getAllObservations()) {
			buildProfileDataset(observation, useCycleNumber, observationCounter);
			observationCounter++;
		    }
		}
		// if all observations mode, display all observations of the dataset
		else if (superposedModeEnum == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET) {
		    // determine si tous les cycle numbers sont égaux ou pas (dans ce cas on utilise
		    // l'observationCounter)
		    useCycleNumber = checkCycleNumber(dataset.getObservations());
		    int observationCounter = 0;
		    for (final Observation observation : dataset.getObservations()) {
			buildProfileDataset(observation, useCycleNumber, observationCounter);
			observationCounter++;
		    }
		}
		// if current obs only
		else {
		    buildProfileDataset(dataset.getObservations().get(Integer.parseInt(currentObservationIndex) - 1),
			    true, null);
		}
	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	}
    }

    private boolean checkCycleNumber(final List<Observation> obs) {
	boolean localUseCycleNumber = false;
	Integer lastValue = null;
	for (final Observation currentObs : obs) {
	    if ((lastValue != null) && (!lastValue.equals(currentObs.getMetadata("CV_NUMBER").getValue()))) {
		localUseCycleNumber = true;
		break;
	    }
	    lastValue = (Integer) currentObs.getMetadata("CV_NUMBER").getValue();
	}

	return localUseCycleNumber;
    }

    /**
     * Change les données du dataframe courant à partir du modèle de données scoop
     *
     * @param dataset
     *            le modèle de données scoop
     */
    private void loadTempDataSet(final Dataset dataset, final String xJComboBox, final String yJComboBox,
	    final String zJComboBox, final String colorMapJComboBox) {
	try {
	    if (parameterKeySet.size() <= 4) {
		if (dataframe.get(xJComboBox) == null) {
		    dataframe.put(xJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(xJComboBox);
		if (dataframe.get(yJComboBox) == null) {
		    dataframe.put(yJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(yJComboBox);
		if (dataframe.get(zJComboBox) == null) {
		    dataframe.put(zJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(zJComboBox);
		if (dataframe.get(colorMapJComboBox) == null) {
		    dataframe.put(colorMapJComboBox, new ArrayList<Double>());
		}
		parameterKeySet.add(colorMapJComboBox);
	    }

	    // si le dataset.type == TIMESERIE, on a besoin de time et des oceanicParameters
	    if (dataset.getDatasetType() == DatasetType.TIMESERIE) {
		totalSize = 0;
		for (final Observation observation : dataset.getObservations()) {
		    Boolean badQC = false;
		    for (int i = 0; i < observation.getReferenceParameter().getDimension(); i++) {
			for (final String p : parameterKeySet) {
			    if (observation.getOceanicParameters().containsKey(p)) {
				if ((observation.getOceanicParameters().get(p) != null)
					&& (observation.getOceanicParameters().get(p).getValues() != null)
					&& !badQC.booleanValue()
					&& ((observation.getOceanicParameter(p).getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getOceanicParameter(p).getQcValues()
							.get(i) == QCValues.QC_4))) {
				    badQC = true;// un index d'observation n'est pas retenu si au moins un QC dans
						 // les parametres = 3 ou 4
				}
				if (Double.isInfinite(observation.getOceanicParameters().get(p).getValues().get(i))
					|| Double.isNaN(observation.getOceanicParameters().get(p).getValues().get(i))) {
				    badQC = true;
				}
			    } else {
				if (!badQC.booleanValue()) {
				    if (p.equals(LONGITUDE_KEY)) {
					if ((observation.getLongitude().getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getLongitude().getQcValues().get(i) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(LATITUDE_KEY)) {
					if ((observation.getLatitude().getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getLatitude().getQcValues().get(i) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(TIME_KEY)
					    && ((observation.getTime().getQcValues().get(i) == QCValues.QC_3)
						    || (observation.getTime().getQcValues().get(i) == QCValues.QC_4))) {
					badQC = true;
				    }
				}
			    }
			}
			// si les QC sont différents de 3 ou 4, on
			// ajoute l'index d'observation au
			// dataframe
			if ((!badQC.booleanValue())
				&& (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[0])
					|| parameterKeySet.toArray()[0].equals(TIME_KEY)
					|| parameterKeySet.toArray()[0].equals(LONGITUDE_KEY)
					|| parameterKeySet.toArray()[0].equals(LATITUDE_KEY))
				&& (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[1])
					|| parameterKeySet.toArray()[1].equals(TIME_KEY)
					|| parameterKeySet.toArray()[1].equals(LONGITUDE_KEY)
					|| parameterKeySet.toArray()[1].equals(LATITUDE_KEY))
				&& (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[2])
					|| parameterKeySet.toArray()[2].equals(TIME_KEY)
					|| parameterKeySet.toArray()[2].equals(LONGITUDE_KEY)
					|| parameterKeySet.toArray()[2].equals(LATITUDE_KEY))
				&& (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[3])
					|| parameterKeySet.toArray()[3].equals(TIME_KEY)
					|| parameterKeySet.toArray()[3].equals(LONGITUDE_KEY)
					|| parameterKeySet.toArray()[3].equals(LATITUDE_KEY))) {
			    int cpt = 0;
			    for (final String p : parameterKeySet) {
				if (cpt < dataframe.keySet().size()) {
				    if (p.equals(LONGITUDE_KEY)) {
					dataframe.get(LONGITUDE_KEY).add(observation.getLongitude().getValues().get(0));
				    } else if (p.equals(LATITUDE_KEY)) {
					dataframe.get(LATITUDE_KEY).add(observation.getLatitude().getValues().get(0));
				    } else if (p.equals(TIME_KEY)) {
					dataframe.get(TIME_KEY).add((double) observation.getTime().getValues().get(0));
				    } else {
					dataframe.get(p)
						.add(observation.getOceanicParameters().get(p).getValues().get(0));
				    }
				    cpt++;
				}
			    }
			    totalSize++;
			}
		    }
		}
	    }

	    // si le dataset.type == PROFILE, on a besoin de depth, lon, lat, date et des oceanicParameters
	    if (dataset.getDatasetType() == DatasetType.PROFILE) {
		totalSize = 0;
		for (final Observation observation : dataset.getObservations()) {
		    Boolean badQC = false;
		    for (int i = 0; i < observation.getReferenceParameter().getDimension(); i++) {
			for (final String p : parameterKeySet) {
			    if (observation.getOceanicParameters().containsKey(p)) {
				if ((observation.getOceanicParameters().get(p) != null)
					&& (observation.getOceanicParameters().get(p).getValues() != null)
					&& !badQC.booleanValue()
					&& ((observation.getOceanicParameter(p).getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getOceanicParameter(p).getQcValues()
							.get(i) == QCValues.QC_4))) {
				    badQC = true;// un index d'observation n'est pas retenu si au moins un QC dans
						 // les parametres = 3 ou 4
				}
				if (Double.isInfinite(observation.getOceanicParameters().get(p).getValues().get(i))
					|| Double.isNaN(observation.getOceanicParameters().get(p).getValues().get(i))) {
				    badQC = true;
				}
			    } else {
				if (!badQC.booleanValue()) {
				    if (p.equals(DEPTH_KEY)) {
					if ((observation.getZ().getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getZ().getQcValues().get(i) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(PRES_KEY)) {
					if ((observation.getZ().getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getZ().getQcValues().get(i) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(LONGITUDE_KEY)) {
					if ((observation.getLongitude().getQcValues().get(0) == QCValues.QC_3)
						|| (observation.getLongitude().getQcValues().get(0) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(LATITUDE_KEY)) {
					if ((observation.getLatitude().getQcValues().get(0) == QCValues.QC_3)
						|| (observation.getLatitude().getQcValues().get(0) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(DATE_KEY)) {
					if ((observation.getFirstDateTimeClone().getQc() == QCValues.QC_3)
						|| (observation.getFirstDateTimeClone().getQc() == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (p.equals(TIME_KEY)) {
					if ((observation.getTime().getQcValues().get(i) == QCValues.QC_3)
						|| (observation.getTime().getQcValues().get(i) == QCValues.QC_4)) {
					    badQC = true;
					}
				    } else if (!observation.getOceanicParameters().containsKey(p)) {
					badQC = true;
				    }
				}
			    }
			}
			if (!badQC.booleanValue()) {
			    int cpt = 0;
			    for (final String p : parameterKeySet) {
				if (cpt < dataframe.keySet().size()) {
				    if (p.equals(DEPTH_KEY)) {
					dataframe.get(DEPTH_KEY).add(observation.getZ().getValues().get(0));
				    } else if (p.equals(PRES_KEY)) {
					dataframe.get(PRES_KEY).add(observation.getZ().getValues().get(0));
				    } else if (p.equals(LONGITUDE_KEY)) {
					dataframe.get(LONGITUDE_KEY).add(observation.getLongitude().getValues().get(0));
				    } else if (p.equals(LATITUDE_KEY)) {
					dataframe.get(LATITUDE_KEY).add(observation.getLatitude().getValues().get(0));
				    } else if (p.equals(DATE_KEY)) {
					dataframe.get(DATE_KEY)
						.add((double) observation.getFirstDateTimeClone().getValueAsLong());
				    } else if (p.equals(CYCLE_NUMBER)) {
					dataframe.get(CYCLE_NUMBER).add(
						(double) (Integer) observation.getMetadata("CV_NUMBER").getValue());
				    } else {
					dataframe.get(p)
						.add(observation.getOceanicParameters().get(p).getValues().get(0));
				    }
				    cpt++;
				}
			    }
			    totalSize++;
			}
		    }
		}
	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * getter pour le nombre de lignes du dataframe
     *
     * @return le nombre de ligne du dataframe
     */
    public int getRowCount() {
	return totalSize;
    }

    /**
     * Getter pour une valeur d'une colonne du dataframe
     *
     * @param key
     *            la colonne dont on veut une valeur
     * @param index
     *            l'indice de la valeur souhaitée
     * @return
     */
    public double getValue(final String key, final int index) {
	return dataframe.get(key).get(index);
    }

    /**
     * Getter des valeurs d'une colonne du dataframe
     *
     * @param key
     *            la colonne dont on veut les valeurs
     * @return les valeurs de la colonne
     */
    public double[] getArray(final String key) {
	final double[] doubles = new double[dataframe.get(key).size()];
	int i = 0;
	for (final double d : dataframe.get(key)) {
	    doubles[i] = d;
	    i++;
	}
	return doubles;
    }

    public void buildTimeSerieDataset(final Observation observation) {
	final Boolean badQC = false;
	for (int i = 0; i < observation.getReferenceParameter().getDimension(); i++) {
	    // for (final String p : parameterKeySet) {
	    // if
	    // (observation.getOceanicParameters().containsKey(p)) {
	    // if ((observation.getOceanicParameters().get(p) !=
	    // null)
	    // &&
	    // (observation.getOceanicParameters().get(p).getValues()
	    // != null) && !badQC) {
	    // if
	    // ((observation.getOceanicParameter(p).getQcValues().get(i)
	    // == QCValues.QC_3)
	    // || (observation.getOceanicParameter(p).getQcValues()
	    // .get(i) == QCValues.QC_4)) {
	    // badQC = true;// un index d'observation
	    // // n'est pas retenu si
	    // // au moins un QC dans
	    // // les parametres = 3 ou
	    // // 4
	    // }
	    // }
	    // if
	    // (Double.isInfinite(observation.getOceanicParameters().get(p).getValues().get(i))
	    // ||
	    // Double.isNaN(observation.getOceanicParameters().get(p).getValues().get(i)))
	    // {
	    // badQC = true;
	    // }
	    // } else {
	    // if (!badQC) {
	    // if (p.equals(LONGITUDE_KEY)) {
	    // if ((observation.getLongitude().getQcValues().get(i)
	    // == QCValues.QC_3)
	    // || (observation.getLongitude().getQcValues().get(i)
	    // == QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if (p.equals(LATITUDE_KEY)) {
	    // if ((observation.getLatitude().getQcValues().get(i)
	    // == QCValues.QC_3)
	    // || (observation.getLatitude().getQcValues().get(i) ==
	    // QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if (p.equals(TIME_KEY)) {
	    // if ((observation.getTime().getQcValues().get(i) ==
	    // QCValues.QC_3)
	    // || (observation.getTime().getQcValues().get(i) ==
	    // QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // }
	    // }
	    // }
	    // }

	    // si les QC sont différents de 3 ou 4, on ajoute l'index d'observation au
	    // dataframe
	    if ((!badQC.booleanValue())
		    && (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[0])
			    || parameterKeySet.toArray()[0].equals(TIME_KEY)
			    || parameterKeySet.toArray()[0].equals(LONGITUDE_KEY)
			    || parameterKeySet.toArray()[0].equals(LATITUDE_KEY))
		    && (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[1])
			    || parameterKeySet.toArray()[1].equals(TIME_KEY)
			    || parameterKeySet.toArray()[1].equals(LONGITUDE_KEY)
			    || parameterKeySet.toArray()[1].equals(LATITUDE_KEY))
		    && (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[2])
			    || parameterKeySet.toArray()[2].equals(TIME_KEY)
			    || parameterKeySet.toArray()[2].equals(LONGITUDE_KEY)
			    || parameterKeySet.toArray()[2].equals(LATITUDE_KEY))
		    && (observation.getOceanicParameters().containsKey(parameterKeySet.toArray()[3])
			    || parameterKeySet.toArray()[3].equals(TIME_KEY)
			    || parameterKeySet.toArray()[3].equals(LONGITUDE_KEY)
			    || parameterKeySet.toArray()[3].equals(LATITUDE_KEY))) {
		int cpt = 0;
		for (final String p : parameterKeySet) {
		    if (cpt < dataframe.keySet().size()) {
			if (p.equals(LONGITUDE_KEY)) {
			    dataframe.get(LONGITUDE_KEY).add(observation.getLongitude().getValues().get(i));
			} else if (p.equals(LATITUDE_KEY)) {
			    dataframe.get(LATITUDE_KEY).add(observation.getLatitude().getValues().get(i));
			} else if (p.equals(TIME_KEY)) {
			    dataframe.get(TIME_KEY).add((double) observation.getTime().getValues().get(i));
			} else {
			    dataframe.get(p).add(observation.getOceanicParameters().get(p).getValues().get(i));
			}
			cpt++;
		    }
		}
		totalSize++;
	    }
	}
    }

    public void buildProfileDataset(final Observation observation, final boolean useCycleNumber,
	    final Integer indexObs) {
	final Boolean badQC = false;
	for (int i = 0; i < observation.getReferenceParameter().getDimension(); i++) {
	    // for (final String p : parameterKeySet) {
	    // if
	    // (observation.getOceanicParameters().containsKey(p)) {
	    // if ((observation.getOceanicParameters().get(p) !=
	    // null)
	    // &&
	    // (observation.getOceanicParameters().get(p).getValues()
	    // != null) && !badQC) {
	    // if
	    // ((observation.getOceanicParameter(p).getQcValues().get(i)
	    // == QCValues.QC_3)
	    // || (observation.getOceanicParameter(p).getQcValues()
	    // .get(i) == QCValues.QC_4)) {
	    // badQC = true;// un index d'observation
	    // // n'est pas retenu si
	    // // au moins un QC dans
	    // // les parametres = 3 ou
	    // // 4
	    // }
	    // }
	    // if
	    // (Double.isInfinite(observation.getOceanicParameters().get(p).getValues().get(i))
	    // ||
	    // Double.isNaN(observation.getOceanicParameters().get(p).getValues().get(i)))
	    // {
	    // badQC = true;
	    // }
	    // } else {
	    // if (!badQC) {
	    // if (p.equals(DEPTH_KEY)) {
	    // if ((observation.getZ().getQcValues().get(i) ==
	    // QCValues.QC_3)
	    // || (observation.getZ().getQcValues().get(i) ==
	    // QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if (p.equals(LONGITUDE_KEY)) {
	    // if ((observation.getLongitude().getQcValues().get(0)
	    // == QCValues.QC_3)
	    // || (observation.getLongitude().getQcValues().get(0)
	    // == QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if (p.equals(LATITUDE_KEY)) {
	    // if ((observation.getLatitude().getQcValues().get(0)
	    // == QCValues.QC_3)
	    // || (observation.getLatitude().getQcValues().get(0) ==
	    // QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if (p.equals(DATE_KEY)) {
	    // if ((observation.getFirstDateTimeClone().getQc() ==
	    // QCValues.QC_3)
	    // || (observation.getFirstDateTimeClone().getQc() ==
	    // QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if (p.equals(TIME_KEY)) {
	    // if ((observation.getTime().getQcValues().get(i) ==
	    // QCValues.QC_3)
	    // || (observation.getTime().getQcValues().get(i) ==
	    // QCValues.QC_4)) {
	    // badQC = true;
	    // }
	    // } else if
	    // (!observation.getOceanicParameters().containsKey(p))
	    // {
	    // badQC = true;
	    // }
	    // }
	    // }
	    // }

	    if (!badQC.booleanValue()) {// si les QC sont différents de 3 ou 4, on ajoute l'index d'observation au
		// dataframe
		boolean currentObsContainsAllParams = true;
		for (final String p : parameterKeySet) {
		    if (!p.equals(DEPTH_KEY) && !p.equals(PRES_KEY) && !p.equals(LONGITUDE_KEY)
			    && !p.equals(LATITUDE_KEY) && !p.equals(DATE_KEY) && !p.equals(CYCLE_NUMBER)
			    && (observation.getOceanicParameters().get(p) == null)) {
			currentObsContainsAllParams = false;
		    }
		}
		if (currentObsContainsAllParams) {
		    int cpt = 0;
		    for (final String p : parameterKeySet) {
			if (cpt < dataframe.keySet().size()) {
			    if (p.equals(DEPTH_KEY)) {
				dataframe.get(DEPTH_KEY).add(observation.getZ().getValues().get(i));
			    } else if (p.equals(PRES_KEY)) {
				dataframe.get(PRES_KEY).add(observation.getZ().getValues().get(i));
			    } else if (p.equals(LONGITUDE_KEY)) {
				dataframe.get(LONGITUDE_KEY).add(observation.getLongitude().getValues().get(0));
			    } else if (p.equals(LATITUDE_KEY)) {
				dataframe.get(LATITUDE_KEY).add(observation.getLatitude().getValues().get(0));
			    } else if (p.equals(DATE_KEY)) {
				dataframe.get(DATE_KEY)
					.add((double) observation.getFirstDateTimeClone().getValueAsLong());
			    } else if (p.equals(CYCLE_NUMBER)) {
				if (useCycleNumber) {
				    dataframe.get(CYCLE_NUMBER)
					    .add((double) (Integer) observation.getMetadata("CV_NUMBER").getValue());
				} else if (!useCycleNumber && (indexObs != null)) {
				    dataframe.get(CYCLE_NUMBER).add((double) indexObs);
				}
			    } else {
				if (observation.getOceanicParameters().get(p) != null) {
				    dataframe.get(p).add(observation.getOceanicParameters().get(p).getValues().get(i));
				}
			    }
			    cpt++;
			}
		    }
		    totalSize++;
		}
	    }
	}
    }

    public boolean getUseCycleNumber() {
	return this.useCycleNumber;
    }
}
