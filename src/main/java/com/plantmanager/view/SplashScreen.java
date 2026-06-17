package com.plantmanager.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Attractive green-and-white splash shown for 3 seconds at startup.
 */
public final class SplashScreen {

    private static final double WIDTH = 520;
    private static final double HEIGHT = 340;

    private SplashScreen() {
    }

    public static void show(Runnable onFinished) {
        Stage splashStage = new Stage(StageStyle.UNDECORATED);
        splashStage.setTitle("Loading...");

        StackPane root = buildSplashContent();
        root.setOpacity(0);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        sequence.setOnFinished(e -> {
            splashStage.close();
            onFinished.run();
        });
        sequence.play();
    }

    private static StackPane buildSplashContent() {
        Rectangle background = new Rectangle(WIDTH, HEIGHT);
        background.setArcWidth(24);
        background.setArcHeight(24);
        background.setFill(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1b4332")),
                new Stop(0.25, Color.web("#2d6a4f")),
                new Stop(0.55, Color.web("#52b788")),
                new Stop(0.78, Color.web("#d8f3dc")),
                new Stop(1, Color.web("#ffffff"))
        ));

        DropShadow panelShadow = new DropShadow(24, Color.rgb(0, 0, 0, 0.35));
        background.setEffect(panelShadow);

        Pane decorations = buildDecorations();

        Text icon = new Text("\uD83C\uDF31");
        icon.setFont(Font.font("System", 56));

        Text title = new Text("Botanical Treatment Advisor");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setFill(Color.web("#ffffff"));
        title.setEffect(textGlow(Color.web("#b7e4c7")));

        Text subtitle = new Text("Track  \u2022  Treat  \u2022  Thrive");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 15));
        subtitle.setFill(Color.web("#eafaf1"));

        Text tagline = new Text("Your garden health companion");
        tagline.setFont(Font.font("System", FontWeight.LIGHT, 13));
        tagline.setFill(Color.web("#f1faee"));

        VBox content = new VBox(10, icon, title, subtitle, tagline);
        content.setAlignment(Pos.CENTER);

        StackPane stack = new StackPane(background, decorations, content);
        stack.setPrefSize(WIDTH, HEIGHT);
        return stack;
    }

    private static Pane buildDecorations() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);
        pane.setPrefSize(WIDTH, HEIGHT);

        addGlowCircle(pane, 420, -40, 140, Color.rgb(255, 255, 255, 0.18));
        addGlowCircle(pane, -60, 220, 160, Color.rgb(255, 255, 255, 0.14));
        addGlowCircle(pane, 300, 260, 90, Color.rgb(183, 228, 199, 0.35));
        addGlowCircle(pane, 80, 40, 50, Color.rgb(255, 255, 255, 0.22));

        Circle accent = new Circle(460, 280, 6, Color.web("#ffffff", 0.85));
        Circle accent2 = new Circle(48, 290, 4, Color.web("#d8f3dc", 0.9));
        Circle accent3 = new Circle(260, 30, 5, Color.web("#95d5b2", 0.8));
        pane.getChildren().addAll(accent, accent2, accent3);

        return pane;
    }

    private static void addGlowCircle(Pane pane, double x, double y, double radius, Color color) {
        Circle circle = new Circle(x, y, radius);
        circle.setFill(new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, color),
                new Stop(1, Color.TRANSPARENT)
        ));
        pane.getChildren().add(circle);
    }

    private static DropShadow textGlow(Color glowColor) {
        DropShadow shadow = new DropShadow(8, glowColor);
        shadow.setSpread(0.15);
        return shadow;
    }
}
