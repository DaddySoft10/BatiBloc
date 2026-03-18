package domaine;

public class ZoneBloc extends Zone {
    private int nombreBlocs = 0;
    private boolean simulationEffectuee = false;

    public ZoneBloc(double x, double y, double largeur, double hauteur, String typeForme) {
        super(x, y, largeur, hauteur, typeForme, "Bloc");
    }

    public ZoneBloc() {
        super();
        setTypeZone("Bloc");
    }

    public int getNombreBlocs() { return nombreBlocs; }
    public void setNombreBlocs(int nombreBlocs) { this.nombreBlocs = nombreBlocs; }

    public boolean isSimulationEffectuee() { return simulationEffectuee; }
    public void setSimulationEffectuee(boolean simulationEffectuee) { this.simulationEffectuee = simulationEffectuee; }
}
