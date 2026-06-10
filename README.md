# Reconnaissance Faciale par ACP
 
Projet Java de reconnaissance faciale basé sur l'Analyse en Composantes Principales (ACP) et la méthode des Eigenfaces, avec une interface graphique JavaFX.
 
**Auteurs** : Danika Theam, Nylan Paillassa, Maël Lescoulié, Virgile Caumont, Soraya Soulez-Damazie
 
---
 
## Principe
 
Le système :
- prend en compte un ensemble de visages de référence
- compare leurs variabilités
- réalise une décomposition en valeurs propres sur la matrice de variance / covariance
- en extrait une base réduite (eigenfaces) en utilisant les principes de la SVD
- compare une image de test aux références dans cet espace réduit en utilisant une distance au choix entre la distance de Mahalanobis, cosinus et euclidienne
- choisit l'image la plus près
- si le visage est trop éloigné de toutes les références, le système retourne "Inconnu" grâce au critère de Hotelling et d'un pourcentage de distances gardé.
 
---
 
## Structure du projet
 
```
Reconnaissance-Faciale/
├── src/
│   └── application/
│       ├── Abstraction/        # Fonctions mathématiques (ACP, SVD, Eigenfaces, Reconnaissance...) et récupération des images (BaseDeDonnees)
│       ├── Controle/           # Gestionnaire central (lien UI / traitement)
│       └── Presentation/       # Interface JavaFX (écrans, panneaux, CSS)
├── donnees/
│   ├── apprentissage/          # Images de référence organisées par dossier/personne
│   ├── base/                   # Deuxième base de référence possible
│   └── test/                   # Images à tester
├── lib/                        # Bibliothèques JAR
└── bin/                        # Classes compilées
```
 
---
 
## Dépendances
 
| Bibliothèque | Rôle |
|---|---|
| EJML 0.45 | Calcul matriciel (SVD, projections) |
| Apache Commons Math 3.6.1 | Loi de Fisher (seuil de Hotelling) |
| JavaFX | Interface graphique, affichage des courbes |
 
---
 
## Lancement
 
### Prérequis
 
- Java 11 ou supérieur
- JavaFX installé (les JARs sont dans `lib/`)
 
### Compilation
 
Sur Linux :
 
```bash
javac -cp "lib/*" -d bin $(find src -name "*.java")
```
 
### Exécution
 
Sur Linux :
 
```bash
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.swing -cp "bin:lib/*" application.MainApp
```
 
---
 
## Utilisation
 
1. Lancer l'application.
2. Il est possible de directement choisir sa base d'apprentissage ("apprentissage" / "base")
4. Le bouton **"Choisir une image"** permet de choisir l'image de test manuellement.
5. Sélectionner la méthode de distance souhaitée (Mahalanobis, euclidienne, cosinus).
6. Cliquer sur **"Lancer la reconnaissance"**.
7. L'image la plus proche et son identité est affichée ainsi que les 5 meilleures correspondances.
 
L'onglet **Visualisation** permet d'afficher le visage moyen, les eigenfaces et la courbe de variance expliquée.
 
---
 
## Base de données d'apprentissage
 
Les images doivent être placées dans un sous-dossier portant le nom de la personne.
 
La base fournie contient 10 personnalités : Alysa Liu, Anok Yai, Bruno Mars, Cristiano Ronaldo, Emma Watson, Lady Gaga, Omar Sy, Philippe Etchebest, Tom Cruise, Zendaya présents dans le sous-dossier suivant :
 
```
donnees/apprentissage/
├── Prénom Nom/
│   ├── prenom1.jpg
│   ├── prenom2.jpg
│   └── ...
```
 
## Base de données de test
 
Certaines données sont présentes dans un répertoire de test :
 
```
donnees/test/
├── 1.jpg
├── 2.jpg
└── ...
```
 
---
 
## Méthodes de distance disponibles
 
- **Euclidienne** : norme 2 dans l'espace des eigenfaces (défaut)
- **Cosinus** : mesure l'angle entre les vecteurs
- **Mahalanobis** : normalise par les valeurs propres, pondère chaque axe par sa variance
 
---
 
## Paramètres clés
 
| Paramètre | Valeur | Description |
|---|---|---|
| Seuil de variance | 95 % | Nombre de composantes retenues pour la base réduite |
| Alpha Hotelling | 0.9 | Seuil de rejet (plus élevé = plus strict) |
 
---
 
## Architecture
 
Le projet suit un modèle MVC simplifié :
 
- **Abstraction** : contient toute la logique mathématique ainsi que le recueil des données (`Acp`, `SVD`, `Eigenfaces`, `Projection`, `Reconnaissance`, `BaseDeDonnees`, `ImageVect`, `Comparaison`, `Propre`)
- **Controle** : `Gestionnaire` organise le chargement, la reconnaissance et la mise à jour de l'interface, donc récupère les fonctions dans Abstraction pour les mettre dans Presentation
- **Presentation** : `Ecran`, `MenuLateral`, `PanneauReconnaissance`, `PanneauVisualisation`, `ConteneurPrincipal`
 
