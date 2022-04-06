package com.fisagroup.test1.trains;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GraphService {
    private final Map<Character, List<EdgeModel>> lineMap;
    private static final String JOIN_CHAR = "-";

    /**
     * ([A-Z]{2}[0-9],?)+
     * Construye los puntos de conexión y diagramas de líneas
     *
     * @param graph like "AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7"
     */
    public GraphService(String graph) {
        final Pattern r = Pattern.compile("^[A-Z]{2}[0-9]$");
        this.lineMap = Pattern.compile(",")
                .splitAsStream(graph)
                .filter(x -> r.matcher(x).matches())
                .distinct()
                .map(x -> new EdgeModel(x.charAt(0), x.charAt(1), Character.getNumericValue(x.charAt(2))))
                .collect(Collectors.groupingBy(EdgeModel::getStartPoint));
    }

    /**
     * Calcula la longitud de la ruta, método de cálculo 1-5 acumulación de bucle.
     * @param route
     * @return
     */
    public int routeLength(String route) {
        String[] points = route.split(JOIN_CHAR);
        // A-B-C
        int result = 0;
        for (int i = 0; i < points.length - 1
                ; i++) {
            char start = points[i].charAt(0);
            char end = points[i + 1].charAt(0);
            int length = this.getLineLength(start, end);
            if (length == -1) return -1;
            result += length;
        }
        return result;
    }

    /**
     * calcula la cantidad de rutas alcanzables que satisfacen la cantidad máxima de paradas
     * método que calcula el size
     * @param startPoint
     * @param endPoint
     * @param stops
     * @return
     */
    public int routeCountStopsSize(char startPoint, char endPoint, int stops) {
        List<String> result= this.maximumStops(startPoint, endPoint, stops,startPoint+"", new ArrayList<>());
        return result.size();
    }

    /**
     * calcula la cantidad de rutas accesibles que satisfacen la cantidad especificada de paradas
     * @param startPoint
     * @param endPoint
     * @param stops
     * @return
     */
    public int routeEqualsStopsSize(char startPoint, char endPoint, int stops) {
        List<String> result = this.equalsStops(startPoint, endPoint, stops, startPoint+"",new ArrayList<>());
        return result.size();
    }

    /**
     * calcula la longitud del camino más corto (idea, el camino más corto no excederá todos los nodos, adecuado para cuando hay pocos nodos...)
     * @param startPoint
     * @param endPoint
     * @return
     */
    public int routeShortest(char startPoint, char endPoint) {
        Map<Character,Integer> maps = new HashMap<>();
        lineMap.keySet().forEach(it->maps.put(it,Integer.MAX_VALUE));

        LinkedList<Character> link = new LinkedList();

        lineMap.get(startPoint).forEach(it->{
            maps.put(it.getEndPoint(),it.getLength());
            link.push(it.getEndPoint());
        });
        while (!link.isEmpty()){
            Character point = link.poll();
            lineMap.get(point).forEach(it->{
                final int length = maps.get(point) + it.getLength();
                if(maps.get(it.getEndPoint()) > length) {
                    maps.put(it.getEndPoint(), length);
                    link.push(it.getEndPoint());
                }
            });
        }
        return maps.get(endPoint);
    }

    /**
     * Calcula el número de líneas con 2 puntos menos que la longitud de línea especificada
     * @param startPoint
     * @param endPoint
     * @param maxLength
     * @return
     */

    public int routLessThenLength(char startPoint, char endPoint, int maxLength) {
        List<String> result = this.lessThenLength(startPoint, endPoint, maxLength,startPoint+"",new ArrayList<>());
        return result.size();
    }


    /**
     * Obtiene todos los caminos que satisfacen la estadía máxima, utilizando la búsqueda recursiva
     *
     * @param startPoint
     * @param endPoint
     * @param stops
     * @return
     */
    private List<String> maximumStops(char startPoint, char endPoint, int stops, String path, List<String> result) {

        if (stops == 0) {
            return result;
        }
        for (EdgeModel edge : lineMap.get(startPoint)) {
            final String nextPath =  path + JOIN_CHAR + edge.getEndPoint();
            if (edge.getEndPoint() == endPoint ) {
                result.add(nextPath);
            } else {
                this.maximumStops(edge.getEndPoint(), endPoint, stops - 1, nextPath, result);
            }
        }
        return result;
    }

    /**
     * Obtiene una ruta que satisfaga la misma cantidad de paradas
     *
     * @param startPoint
     * @param endPoint
     * @param stops
     * @return
     */
    private List<String> equalsStops(char startPoint, char endPoint, int stops, String path, List<String> result) {
        if (stops == 0) {
            return result;
        }
        for (EdgeModel edge : lineMap.get(startPoint)) {
            final String nextPath = path +JOIN_CHAR + edge.getEndPoint();
            if(edge.getEndPoint() == endPoint && stops == 1) {
                result.add(nextPath);
            } else {
                this.equalsStops(edge.getEndPoint(), endPoint, stops -1, nextPath, result);
            }
        }
        return result;
    }

    /**
     * Encuentra todas las rutas con 2 puntos menos que la longitud de ruta especificada
     *
     * @param startPoint
     * @param endPoint
     * @return
     */
    private List<String> lessThenLength(char startPoint, char endPoint, int left,String path, List<String> result) {
        if(left < 0) return result;

        for (EdgeModel edge : lineMap.get(startPoint)) {
            final String nextPath = path + JOIN_CHAR + edge.getEndPoint();
            if(edge.getEndPoint() == endPoint && left > edge.getLength()) {
                result.add(nextPath);
            }
            this.lessThenLength(edge.getEndPoint(), endPoint, left - edge.getLength(), nextPath, result);
        }
        return result;
    }

    /**
     * obtiene la longitud de 2 puntos
     *
     * @param start 起点
     * @param end   终点
     * @return
     */
    private int getLineLength(char start, char end) {
        return lineMap.values().stream().flatMap(Collection::stream)
                .filter(x -> x.getStartPoint() == start && x.getEndPoint() == end)
                .findAny().map(EdgeModel::getLength).orElse(-1);
    }

}
