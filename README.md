# Reconnaissance Faciale par ACP / Eigenfaces

Projet Java de reconnaissance faciale basé sur l'Analyse en Composantes Principales (ACP) et la méthode des Eigenfaces, avec une interface graphique JavaFX.

**Auteurs** : Danika Theam, Nylan Paillassa, Maël Lescoulié, Virgile Caumont, Soraya Soulez-Damazie

---

## Principe

Le système apprend un ensemble de visages de référence, en extrait une base réduite (eigenfaces) par décomposition SVD, puis identifie un nouveau visage en le comparant aux références dans cet espace réduit. Si le visage est trop éloigné de toutes les références, le système retourne "Inconnu" grâce au critère de Hotelling T².

---

## Structure du projet

```
Reconnaissance-Faciale/
├── src/
│   └── application/
│       ├── Abstraction/        # Logique métier (ACP, SVD, Eigenfaces, Reconnaissance...)
│       ├── Controle/           # Gestionnaire central (lien UI / traitement)
│       └── Presentation/       # Interface JavaFX (écrans, panneaux, CSS)
├── donnees/
│   ├── apprentissage/          # Images de référence organisées par dossier/personne
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

```bash
javac -cp "lib/*" -d bin $(find src -name "*.java")
```

### Exécution

```bash
java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.swing -cp "bin:lib/*" application.MainApp
```

---

## Utilisation

1. Lancer l'application.
2. Choisir une image (JPG, PNG ou PGM) via le bouton **"Choisir une image"**.
3. Sélectionner la méthode de distance souhaitée.
4. Cliquer sur **"Lancer la reconnaissance"**.
5. Le résultat s'affiche avec l'identité trouvée et les 5 meilleures correspondances.

L'onglet **Visualisation** permet d'afficher le visage moyen, les eigenfaces et la courbe de variance expliquée.

---

## Base de données d'apprentissage

Les images doivent être placées dans `donnees/apprentissage/` dans un sous-dossier portant le nom de la personne :

```
donnees/apprentissage/
├── Prénom Nom/
│   ├── Prénom Nom1.jpg
│   ├── Prénom Nom2.jpg
│   └── ...
```

## Base de données de test

La base fournie contient 10 personnalités : Alysa Liu, Anok Yai, Bruno Mars, Cristiano Ronaldo, Emma Watson, Lady Gaga, Omar Sy, Philippe Etchebest, Tom Cruise, Zendaya présents dans le sous-dossier suivant :
```
donnees/test/
├── 1.jpg
├── 2.jpg
└── ...
```

---

## Méthodes de distance disponibles

- **Euclidienne** : distance L2 dans l'espace des eigenfaces (défaut)
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

- **Abstraction** : contient toute la logique mathématique (`Acp`, `SVD`, `Eigenfaces`, `Projection`, `Reconnaissance`, `BaseDeDonnees`, `ImageVect`, `Comparaison`, `Propre`)
- **Controle** : `Gestionnaire` orchestre le chargement, la reconnaissance et la mise à jour de l'interface
- **Presentation** : `Ecran`, `MenuLateral`, `PanneauReconnaissance`, `PanneauVisualisation`, `ConteneurPrincipal`
