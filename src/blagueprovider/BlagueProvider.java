/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blagueprovider;

import blague.Blague;
import codebase.BlagueProviderPairApair;
import exception.BlagueAbsenteException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author blaise
 */
public class BlagueProvider implements BlagueProviderPairApair {

    /**
     * Le nom du BlagueProvider
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    private final String nom;

    /**
     * Une hashmap contenant les blagues
     */
    private HashMap<String, Blague> listeBlagues;

    /**
     * le nom des autres BlagueProvider connus à leurs références distantes (ces
     * références sont à récupérer au lancement de l’application)
     */
    private HashMap<String, BlagueProvider> listeRef;

    /**
     * Constructeur.
     *
     * @param nom
     */
    public BlagueProvider(String nom) {
        this.nom = nom;
    }

    /**
     * Méthode qui permet d’ajouter une blague en local
     *
     * @param b
     */
    public void ajoutBlague(Blague b) {

        //On ajoute b à la hashmap
        listeBlagues.put(b.getNom(), b);
    }

    /**
     * Ajout d'une référence à la liste.
     *
     * @param name
     * @param refname
     */
    public void ajoutReference(String name, BlagueProvider refname) {
        listeRef.put(name, refname);
    }

    /**
     * Méthode qui télécharge une blague en l'ajoutant à la réf distante.
     *
     * @param ref
     * @param nomBlague
     */
    public void telechargeBlague(BlagueProvider ref, String nomBlague) {

        try {
            //On récupère la blague
            Blague b = getBlague(nomBlague);

            //On l'ajoute à la référence distance
            ref.ajoutBlague(b);

        } catch (BlagueAbsenteException ex) {
            Logger.getLogger(BlagueProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public String getNom() {
        return nom;
    }

    /**
     * Retourne tous les noms des blagues
     *
     * @return
     */
    @Override
    public String[] getAllNames() {
        //Création du tableau résultat
        String[] res = new String[listeBlagues.size()];

        //Initialisation iterateur
        Set cles = listeBlagues.keySet();
        Iterator it = cles.iterator();
        int iterateurRes = 0;

        //Parcourt de la hashmap
        while (it.hasNext()) {
            //Récupère la clé
            String cle = (String) it.next();

            //Ajout de la clé au tableau
            res[iterateurRes] = cle;
            iterateurRes++;
        }

        return res;
    }

    /**
     * Méthode qui construit return l'objet blague à partir de son nom.
     *
     * @param nom
     * @return
     * @throws BlagueAbsenteException
     */
    @Override
    public Blague getBlague(String nom) throws BlagueAbsenteException {

        //On vérifie si la clé existe dans la hashmap
        if (listeBlagues.containsKey(nom)) {
            //On récupère la valeur
            Blague blague = listeBlagues.get(nom);
            return blague;
        } else {
            //On lève l'exception
            throw new BlagueAbsenteException();
        }

    }

    /**
     * Méthode principale.
     *
     * @param args
     */
    public static void main(String[] args) {
        //On test args
        if (args.length < 1) {
            System.out.println("Erreur : Manque un argument !");
        } else {
            //Création de l'objet
            BlagueProvider bp = new BlagueProvider(args[0]);

            // Ajout des références
            //contact avec le rmiregistry de host
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry();

                for (int i = 1; i < args.length; i++) {
                    // Récuperation de la reference distante
                    BlagueProviderPairApair proxy = (BlagueProviderPairApair) registry.lookup(args[i]);

                    // Ajout de la référence
                    bp.ajoutReference(args[i], (BlagueProvider) proxy);
                }

                //Export
                BlagueProviderPairApair ri = (BlagueProviderPairApair) UnicastRemoteObject.exportObject(bp, 0);
                registry.rebind("Client", ri);

            } catch (RemoteException | NotBoundException ex) {
                Logger.getLogger(BlagueProvider.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public HashMap<String, Blague> getListeBlagues() {
        return listeBlagues;
    }

    public void setListeBlagues(HashMap<String, Blague> listeBlagues) {
        this.listeBlagues = listeBlagues;
    }

    public HashMap<String, BlagueProvider> getListeRef() {
        return listeRef;
    }

    public void setListeRef(HashMap<String, BlagueProvider> listeRef) {
        this.listeRef = listeRef;
    }

}
