package services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleMapsService {

    public Document getDocument(LatLng start, LatLng end) {
        String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=driving";
        Log.v("DURATION", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            InputStream in = response.body().byteStream();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(in);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDurationText(Document doc) {
        try {
            Log.v("DURATION TEST", "doc is " + doc.toString());
            NodeList nl1 = doc.getElementsByTagName("duration");
            Log.v("DURATION TEST", "nl1 is " + nl1.toString());
            Node node1 = nl1.item(0);
            Log.v("DURATION TEST", "node1 is " + node1.toString());
            NodeList nl2 = node1.getChildNodes();
            Log.v("DURATION TEST", nl2.toString());
            Node node2 = nl2.item(getNodeIndex(nl2, "text"));
            Log.v("DURATION TEST", "node2 is " + node2.toString());
            Log.i("DURATION TEST", node2.getTextContent());
            return node2.getTextContent();
        } catch (Exception e) {
            return "0";
        }
    }

    public int getDurationValue(Document doc) {
        int totalDuration = 0;
        try {
            NodeList statusNodeList = doc.getElementsByTagName("status");
            if(!statusNodeList.item(0).getTextContent().equals("OK")) return -1;
            NodeList durationNodeList = doc.getElementsByTagName("duration");
            if(durationNodeList.getLength() == 0) return -1;
            for (int i = 0; i < durationNodeList.getLength(); i++) {
                Node durationNode = durationNodeList.item(i);
                NodeList durationNodeChildNodes = durationNode.getChildNodes();
                Node valueNode = durationNodeChildNodes.item(getNodeIndex(durationNodeChildNodes, "value"));
                totalDuration += Integer.parseInt(valueNode.getTextContent());
            }

            return totalDuration / 60;
        } catch (Exception e) {
            return -1;
        }
    }

    public String getDistanceText(Document doc) {
        try {
            NodeList nl1;
            nl1 = doc.getElementsByTagName("distance");

            Node node1 = nl1.item(nl1.getLength() - 1);
            NodeList nl2 = null;
            nl2 = node1.getChildNodes();
            Node node2 = nl2.item(getNodeIndex(nl2, "value"));
            Log.d("DistanceText", node2.getTextContent());
            return node2.getTextContent();
        } catch (Exception e) {
            return "-1";
        }
    }

    public double getDistanceValue(Document doc) {
        double totalDistance = 0;
        try {
            NodeList statusNodeList = doc.getElementsByTagName("status");
            if(!statusNodeList.item(0).getTextContent().equals("OK")) return -1;
            NodeList distanceNodeList = doc.getElementsByTagName("distance");
            if(distanceNodeList.getLength() == 0) return -1;
            for (int i = 0; i < distanceNodeList.getLength(); i++) {
                Node distanceNode = distanceNodeList.item(i);
                NodeList distanceNodeChildNodes = distanceNode.getChildNodes();
                Node valueNode = distanceNodeChildNodes.item(getNodeIndex(distanceNodeChildNodes, "value"));
                totalDistance += Integer.parseInt(valueNode.getTextContent());
            }

            return totalDistance;
        } catch (Exception e) {
            return -1;
        }
    }

    public String getStartAddress(Document doc) {
        try {
            NodeList nl1 = doc.getElementsByTagName("start_address");
            Node node1 = nl1.item(0);
            Log.i("StartAddress", node1.getTextContent());
            return node1.getTextContent();
        } catch (Exception e) {
            return "-1";
        }

    }

    public String getEndAddress(Document doc) {
        try {
            NodeList nl1 = doc.getElementsByTagName("end_address");
            Node node1 = nl1.item(0);
            Log.i("StartAddress", node1.getTextContent());
            return node1.getTextContent();
        } catch (Exception e) {
            return "-1";
        }
    }

    public ArrayList<LatLng> getDirection(Document doc) {
        NodeList nl1, nl2, nl3;
        ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();

                Node locationNode = nl2
                        .item(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getChildNodes();
                Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.getTextContent());
                Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));

                locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "points"));
                ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                for (int j = 0; j < arr.size(); j++) {
                    listGeopoints.add(new LatLng(arr.get(j).latitude, arr
                            .get(j).longitude));
                }

                locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.getTextContent());
                lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));
            }
        }

        return listGeopoints;
    }

    private int getNodeIndex(NodeList nl, String nodename) {
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }

    public String getAddressFromLatLng(Context context, double latitude, double longitude) {
        List<Address> addresses;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
