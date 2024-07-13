//Gunkeet Mutiana
//ID: 40226566

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Ass3 {
    public static void main(String[] args) {
        KDTree tree = new KDTree();
        List<KDTree.Node> patients= readData(tree);
        Collections.shuffle(patients); //used to randomize/shuffle the list of patients


        int trainSize =450; //this is n, can change it to keep a 4 to 1 ratio.
        int testSize= trainSize/4;
        int k=3; //numbers of neighbors to find to create a diagnosis
        //also in the data set, i removed the ID's of the patients as it was irrelevant to their diagnosis.

        //creating training test
        List<KDTree.Node> trainingSet = new ArrayList<>();
        for (int i = 0; i < trainSize; i++) {
            trainingSet.add(patients.get(i));
        }
        tree = new KDTree();
        for (KDTree.Node patient : trainingSet) {
            tree.insert(patient);
        }

        // creates testing set
        List<KDTree.Node> testingSet = new ArrayList<>();
        for (int i = trainSize; i < trainSize + testSize; i++) {
            testingSet.add(patients.get(i));
        }

        // Performs testing
        test(tree, testingSet, k);
    }

    //reads data from data.csv and returns the list of patients
    static List<KDTree.Node> readData(KDTree tree) {
        // Reading the data.csv file
        List<KDTree.Node> patients = new ArrayList<>();
        String file = "src//data.csv";
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");

                float[] attributes = new float[10]; //i only used the "mean"  values which are the first 10in the data as the attributes
                char diagnosis = row[0].charAt(0);

                for (int i = 1; i <= 10; i++) {
                    attributes[i - 1] = Float.parseFloat(row[i]);
                }


                //this creates the kd tree by setting patient to attributes and the diagnosis
                KDTree.Node patient = new KDTree.Node(attributes, diagnosis);
                patients.add(patient);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return patients;
    }

    static void test(KDTree tree, List<KDTree.Node> testing, int k) {
        int correctPredictions = 0;
        long start = System.currentTimeMillis();
        // Read data.csv for testing
        for(int i=0; i< testing.size(); i++){
            KDTree.Node patient= testing.get(i);

            float[] attributes= patient.attributes;
            char actualDaignosis= patient.diagnosis;

            List<KDTree.Node> neighbors = tree.kNearestNeighbors(attributes, k);
            char predictedDiagnosis = prediction(neighbors);

            // Print results
            System.out.println("Patient " + (i + 1));
            System.out.println("Actual Diagnosis: " + actualDaignosis);
            System.out.println("Predicted Diagnosis: " + predictedDiagnosis);
            System.out.println();

            if (predictedDiagnosis == actualDaignosis) {
                correctPredictions++;
            }
        }
        //to find the execution time
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - start;
        System.out.println("Total execution time: " + executionTime + " ms");
        // Calculates the accuracy of the program
        double accuracy = (double) correctPredictions / testing.size() * 100;
        System.out.println("Accuracy: " + accuracy + "%");
    }

    //to predict the diagnosis of test patient
    static char prediction(List<KDTree.Node> neighbors)
    {
        int M=0;
        int B= 0;
        for(int i= 0; i<neighbors.size(); i++){
            KDTree.Node neighbor =neighbors.get(i);

            char diagnosis= neighbor.diagnosis;

            //counts the "M" diagnosis and increments M
            if (diagnosis == 'M'){
                M++;
            }
            //Counts "B" diagnosis and increments it depending on the how many B it finds.
            else if(diagnosis== 'B'){
                B++;
            }
        }

        if (M > B) {
            return 'M'; // Predict malignant
        } else {
            return 'B'; // Predicts benign
        }
    }
}

class KDTree {

    //this Node represents a patient
    private Node root;

    public static class Node {
        float[] attributes;
        char diagnosis;

        Node left;
        Node right;

        //initializing
        Node(float[] attributes, char diagnosis) {
            this.attributes = attributes;
            this.diagnosis = diagnosis;
            left = null;
            right = null;
        }
    }

    //inserting into the KD-Tree recursively
    public void insert(Node newNode)
    {
        root = insertRecursive(root, newNode, 0);
    }
    //inserting into the KD-Tree

    private Node insertRecursive(Node current, Node newNode, int depth) {
        if (current == null) {
            return newNode;
        }
        int axis = depth % newNode.attributes.length;


        //newNode is representing the data (attributes and diagonis) that is trying to be inserted and is compared to the past/curent node and decides wheter it is going left or right. then the value is alternated for next comparision
        //this code decides wheter to insert left by comparing the current node with the newNode attributes and goes to the left or right depending on value and alternates.
        if (newNode.attributes[axis] < current.attributes[axis]) {
            current.left = insertRecursive(current.left, newNode, depth + 1);
        } else {
            current.right = insertRecursive(current.right, newNode, depth + 1);
        }
        return current;
    }



    private static class NodeDistance {
        Node node;
        float distance;

        NodeDistance(Node node, float distance) {
            this.node = node;
            this.distance = distance;
        }
    }


    private float distance(float[] point1, float[] point2) {
        float sum = 0;
        for (int i = 0; i < point1.length; i++) {
            float diff = point1[i] - point2[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }

    public void NNRecursive(Node current, float[] point, int k, int depth, PriorityQueue<NodeDistance> prio){

        if (current == null) {
            return;
        }

        float distance = distance(point, current.attributes); //distance to current node
        prio.add(new NodeDistance(current,distance)); //adds the node to the prio queue

        int axis= depth % point.length;

        Node next;
        Node second;

        if (point[axis]< current.attributes[axis]){
            next= current.left; //look at left node if the conditions are met
            second = current.right;//llook right
        }
        else{
            next= current.right;//look right
            second= current.left; // look left
        }
        NNRecursive(next, point, k, depth + 1, prio); //makes it so that its recurisvely exploring the next node

    }
    //Functions that funds the kNN
    public List<Node> kNearestNeighbors(float[] point, int k) {
        PriorityQueue<NodeDistance> prio= new PriorityQueue<>(k, new NodeDistanceCompator(point));

        NNRecursive(root, point, k, 0, prio);

        List<Node> result = new ArrayList<>(); //Stors the nearest neighbors

        while(!prio.isEmpty()){
            result.add(0,prio.poll().node); //makes it so the nodes are added to the beginning of the list.
        }

        return result;
    }


    //calculates distance between nodedistance attributes to the point and compares.
    public class NodeDistanceCompator implements Comparator<NodeDistance>{
        public float[] point;

        NodeDistanceCompator(float[] point)
        {
            this.point= point;
        }

        public int compare(NodeDistance one, NodeDistance two){
            float distance1= distance(one.node.attributes, point); //distance of noe one to the point
            float distance2= distance(two.node.attributes, point); //distance of node two to the point
            return Float.compare(distance1, distance2);
        }
    }
}