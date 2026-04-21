package domaine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SimulateurPlacement {
    public static final double BLOC_LARGEUR = 96.0;
    public static final double BLOC_HAUTEUR = 12.0;
    public static final double MIN_RETAILLE = 6.0;
    private static final double EPSILON = 0.0001;

    private SimulateurPlacement() {
    }

    public static List<ZoneBloc.BlocPlace> simulerFacade(List<Zone> zones) {
        List<ZoneBloc.BlocPlace> blocsGlobaux = new ArrayList<>();

        List<ZoneBloc> zonesRect = new ArrayList<>();
        List<ZoneBloc> zonesTri = new ArrayList<>();
        List<ZoneOuverture> ouvertures = new ArrayList<>();

        for (Zone zone : zones) {
            if (zone instanceof ZoneBloc zb) {
                if (zb.getTypeForme() == TypeForme.RECTANGULAIRE) {
                    zonesRect.add(zb);
                } else {
                    zonesTri.add(zb);
                }
            } else if (zone instanceof ZoneOuverture zo) {
                ouvertures.add(zo);
            }
        }

        simulerZonesRectangulaires(zonesRect, ouvertures, blocsGlobaux);

        for (ZoneBloc triZone : zonesTri) {
            simulerZoneTriangulaire(triZone, blocsGlobaux);
        }

        return blocsGlobaux;
    }

    private static void simulerZonesRectangulaires(List<ZoneBloc> zones, List<ZoneOuverture> ouvertures, List<ZoneBloc.BlocPlace> blocsGlobaux) {
        if (zones.isEmpty() && ouvertures.isEmpty()) return;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (ZoneBloc zone : zones) {
            minY = Math.min(minY, zone.getY());
            maxY = Math.max(maxY, zone.getY() + zone.getHauteur());
        }
        for (ZoneOuverture ov : ouvertures) {
            minY = Math.min(minY, ov.getY());
            maxY = Math.max(maxY, ov.getY() + ov.getHauteur());
        }

        if (minY == Double.MAX_VALUE) return;

        int nombreRangees = (int) Math.ceil((maxY - minY) / BLOC_HAUTEUR);
        java.util.Map<Long, Double> retailleParSegment = new java.util.HashMap<>();

        for (int i = 0; i < nombreRangees; i++) {
            double currentY = minY + i * BLOC_HAUTEUR;

            List<Segment> segments = new ArrayList<>();
            for (ZoneBloc zone : zones) {
                if (isInsideY(zone, currentY)) {
                    segments.add(new Segment(zone.getX(), zone.getX() + zone.getLargeur()));
                }
            }
            segments = fusionnerSegments(segments);

            // Soustraire les ouvertures (en passant tous les zones pour le linteau)
            List<Zone> allZones = new ArrayList<>(zones);
            allZones.addAll(ouvertures);
            segments = soustraireOuvertures(segments, allZones, currentY, blocsGlobaux);
            segments.sort(Comparator.comparingDouble(s -> s.debut));

            for (Segment segment : segments) {
                double largeurDispo = segment.fin - segment.debut;
                if (largeurDispo < MIN_RETAILLE) continue;

                long segmentKey = Math.round(segment.debut * 100);
                double retaille = retailleParSegment.getOrDefault(segmentKey, 0.0);

                double x = segment.debut;
                double longueurPremierBloc;

                if (retaille >= MIN_RETAILLE) {
                    longueurPremierBloc = retaille;
                } else {
                    longueurPremierBloc = BLOC_LARGEUR;
                    double espaceRestant = largeurDispo - longueurPremierBloc;
                    if (espaceRestant > 0) {
                        double reste = espaceRestant % BLOC_LARGEUR;
                        if (reste > EPSILON && reste < MIN_RETAILLE) {
                            longueurPremierBloc -= (MIN_RETAILLE - reste);
                        }
                    }
                }

                double longueurCourante = longueurPremierBloc;
                double retailleFinale = 0.0;

                while (x < segment.fin - EPSILON) {
                    double finBloc = x + longueurCourante;
                    if (finBloc > segment.fin + EPSILON) {
                        double longueurReelle = segment.fin - x;
                        retailleFinale = longueurCourante - longueurReelle;
                        if (retailleFinale < MIN_RETAILLE) retailleFinale = 0.0;
                        blocsGlobaux.add(new ZoneBloc.BlocPlace(x, currentY, longueurReelle, BLOC_HAUTEUR, true));
                        x = segment.fin;
                    } else {
                        boolean estCoupe = Math.abs(longueurCourante - BLOC_LARGEUR) > EPSILON;
                        blocsGlobaux.add(new ZoneBloc.BlocPlace(x, currentY, longueurCourante, BLOC_HAUTEUR, estCoupe));
                        x += longueurCourante;
                        longueurCourante = BLOC_LARGEUR;
                    }
                }

                retailleParSegment.put(segmentKey, retailleFinale);
            }
        }
    }

    private static void simulerZoneTriangulaire(ZoneBloc zone, List<ZoneBloc.BlocPlace> blocsGlobaux) {
        double yStart;
        double yEnd = zone.getY() + zone.getHauteur();

        if (zone.getTypeForme() == TypeForme.TRIANGULAIRE_TRONQUEE) {
            double ratioCoupe = zone.getRatioCoupe();
            if (ratioCoupe <= 0.0) ratioCoupe = 0.5;
            yStart = zone.getY() + zone.getHauteur() * ratioCoupe;
        } else {
            yStart = zone.getY();
        }

        int nombreRangees = (int) Math.ceil((yEnd - yStart) / BLOC_HAUTEUR);

        for (int i = 0; i < nombreRangees; i++) {
            double currentY = yStart + i * BLOC_HAUTEUR;
            if (currentY >= yEnd - EPSILON) break;

            Segment seg = segmentTriangulaire(zone, currentY);
            if (seg == null) continue;

            double largeurDispo = seg.fin - seg.debut;
            if (largeurDispo < MIN_RETAILLE) continue;

            // Pas de retaille entre rangées pour les triangles
            double x = seg.debut;
            double longueurPremierBloc = BLOC_LARGEUR;
            double espaceRestant = largeurDispo - longueurPremierBloc;
            if (espaceRestant > 0) {
                double reste = espaceRestant % BLOC_LARGEUR;
                if (reste > EPSILON && reste < MIN_RETAILLE) {
                    longueurPremierBloc -= (MIN_RETAILLE - reste);
                }
            }

            double longueurCourante = longueurPremierBloc;

            while (x < seg.fin - EPSILON) {
                double finBloc = x + longueurCourante;
                if (finBloc > seg.fin + EPSILON) {
                    double longueurReelle = seg.fin - x;
                    blocsGlobaux.add(new ZoneBloc.BlocPlace(x, currentY, longueurReelle, BLOC_HAUTEUR, true));
                    x = seg.fin;
                } else {
                    boolean estCoupe = Math.abs(longueurCourante - BLOC_LARGEUR) > EPSILON;
                    blocsGlobaux.add(new ZoneBloc.BlocPlace(x, currentY, longueurCourante, BLOC_HAUTEUR, estCoupe));
                    x += longueurCourante;
                    longueurCourante = BLOC_LARGEUR;
                }
            }
        }
    }

    private static Segment segmentTriangulaire(ZoneBloc zone, double currentY) {
        double ratio = (currentY - zone.getY()) / zone.getHauteur();
        double largeurActuelle = zone.getLargeur() * ratio;
        double offset = (zone.getLargeur() - largeurActuelle) / 2.0;
        if (largeurActuelle < MIN_RETAILLE) return null;
        return new Segment(zone.getX() + offset, zone.getX() + offset + largeurActuelle);
    }

    private static boolean isInsideY(Zone zone, double y) {
        return y >= zone.getY() - EPSILON && y < zone.getY() + zone.getHauteur() - EPSILON;
    }

    private static List<Segment> fusionnerSegments(List<Segment> segments) {
        if (segments.isEmpty()) return segments;
        segments.sort(Comparator.comparingDouble(s -> s.debut));
        List<Segment> fusionnes = new ArrayList<>();
        Segment actuel = segments.get(0);

        for (int i = 1; i < segments.size(); i++) {
            Segment s = segments.get(i);
            if (s.debut <= actuel.fin + EPSILON) {
                actuel.fin = Math.max(actuel.fin, s.fin);
            } else {
                fusionnes.add(actuel);
                actuel = s;
            }
        }
        fusionnes.add(actuel);
        return fusionnes;
    }

    private static List<Segment> soustraireOuvertures(List<Segment> segments, List<Zone> zones, double currentY, List<ZoneBloc.BlocPlace> blocsGlobaux) {
        List<Segment> result = new ArrayList<>(segments);
        for (Zone zone : zones) {
            if (zone instanceof ZoneOuverture) {
                boolean isSmallOuverture = zone.getLargeur() <= 84.0;

                if (isInsideY(zone, currentY)) {
                    double oStart = zone.getX();
                    double oEnd = zone.getX() + zone.getLargeur();

                    if (!isSmallOuverture) {
                        oStart -= 4.0;
                        oEnd += 4.0;
                    }

                    result = couperSegments(result, oStart, oEnd);
                } else if (isSmallOuverture && currentY >= zone.getY() + zone.getHauteur() - EPSILON && currentY < zone.getY() + zone.getHauteur() + BLOC_HAUTEUR - EPSILON) {
                    double linteauWidth = zone.getLargeur() + 12.0;
                    linteauWidth = Math.max(linteauWidth, MIN_RETAILLE);

                    double lStart = zone.getX() + zone.getLargeur() / 2.0 - linteauWidth / 2.0;
                    double lEnd = lStart + linteauWidth;

                    blocsGlobaux.add(new ZoneBloc.BlocPlace(lStart, currentY, linteauWidth, BLOC_HAUTEUR, false));
                    result = couperSegments(result, lStart, lEnd);
                }
            }
        }
        return result;
    }

    private static List<Segment> couperSegments(List<Segment> segments, double cutStart, double cutEnd) {
        List<Segment> result = new ArrayList<>();
        for (Segment s : segments) {
            if (cutEnd <= s.debut + EPSILON || cutStart >= s.fin - EPSILON) {
                result.add(s);
            } else {
                if (cutStart > s.debut + EPSILON) {
                    result.add(new Segment(s.debut, cutStart));
                }
                if (cutEnd < s.fin - EPSILON) {
                    result.add(new Segment(cutEnd, s.fin));
                }
            }
        }
        return result;
    }

    private static class Segment {
        double debut;
        double fin;
        Segment(double debut, double fin) {
            this.debut = Math.min(debut, fin);
            this.fin = Math.max(debut, fin);
        }
    }
}
