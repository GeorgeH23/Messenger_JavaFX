package com.chatApplication.chatClient.gui.handlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class ImageCroppingHandler {
    private RubberBandSelection rubberBandSelection;
    private ImageView imageView;
    private String croppedImagePath;
    private Stage primaryStage;

    private static ImageCroppingHandler instance = new ImageCroppingHandler();

    private ImageCroppingHandler() {
    }

    public static ImageCroppingHandler getInstance() {
        return instance;
    }

    public void pictureCropper(Stage stage, String filePath) {
        start(stage, filePath);
    }

    public final String getCroppedImagePath() {
        return this.croppedImagePath;
    }

    private void start(Stage stage, String filePath) {

        this.primaryStage = stage;
        this.primaryStage.setTitle("Image Cropping");
        BorderPane root = new BorderPane();
        // image layer: a group of images
        Group imageLayer = new Group();
        // load the image
        //Image image = new Image( "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Gatto_europeo4.jpg/1024px-Gatto_europeo4.jpg");
        File file = new File(filePath);
        Image image = null;
        try {
            image = new Image(file.toURI().toURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // the container for the image as a javafx node
        imageView = new ImageView( image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitWidthProperty().bind(root.widthProperty());
        imageView.fitHeightProperty().bind(root.heightProperty());

        // add image to layer
        imageLayer.getChildren().add(imageView);
        // put scrollpane in scene
        root.setCenter(imageLayer);
        // rubberband selection
        rubberBandSelection = new RubberBandSelection(imageLayer);
        // create context menu and menu items
        ContextMenu contextMenu = new ContextMenu();
        MenuItem cropMenuItem = new MenuItem("Crop");
        cropMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                // get bounds for image crop
                Bounds selectionBounds = rubberBandSelection.getBounds();
                // show bounds info
                System.out.println( "Selected area: " + selectionBounds);
                // crop the image
                crop(selectionBounds);
            }
        });
        contextMenu.getItems().add( cropMenuItem);

        // set context menu on image layer
        imageLayer.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(imageLayer, event.getScreenX(), event.getScreenY());
                }
            }
        });

        this.primaryStage.setScene(new Scene(root, 800, 800));
        this.primaryStage.show();
    }

    private void crop( Bounds bounds) {

        File file = null;
        try {
            file = File.createTempFile("croppedPicture", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file != null) {
            file.deleteOnExit();
        }

        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( bounds.getMinX(), bounds.getMinY(), width, height));

        WritableImage wi = new WritableImage( width, height);
        imageView.snapshot(parameters, wi);

        // save image (without alpha)
        // --------------------------------
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {
            if (file != null) {
                ImageIO.write(bufImageRGB, "jpg", file);
                croppedImagePath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();
        primaryStage.close();

    }

    /**
     * Drag rectangle with mouse cursor in order to get selection bounds
     */
    private static class RubberBandSelection {

        final DragContext dragContext = new DragContext();
        Circle circle;

        Group group;


        public Bounds getBounds() {
            return circle.getBoundsInParent();
        }

        public RubberBandSelection( Group group) {

            this.group = group;

            circle = new Circle(0,0,0);
            circle.setStroke(Color.BLUE);
            circle.setStrokeWidth(1);
            circle.setStrokeLineCap(StrokeLineCap.ROUND);
            circle.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                // remove old rect
                circle.setCenterX(event.getX());
                circle.setCenterY(event.getY());
                circle.setRadius(0);

                group.getChildren().remove(circle);

                // prepare new drag operation
                dragContext.mouseAnchorX = event.getX();
                dragContext.mouseAnchorY = event.getY();

                circle.setCenterX(dragContext.mouseAnchorX);
                circle.setCenterY(dragContext.mouseAnchorY);
                circle.setRadius(0);

                group.getChildren().add(circle);
            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                double offsetX = event.getX() - dragContext.mouseAnchorX;
                double offsetY = event.getY() - dragContext.mouseAnchorY;
                double min = Math.min(group.getScene().getWidth(), group.getScene().getHeight());

                if (Math.abs(offsetX) < min/2 && Math.abs(offsetY) < min/2) {
                    if (Math.abs(offsetX) > Math.abs(offsetY)) {
                        circle.setRadius(Math.abs(offsetX));
                    } else {
                        circle.setRadius(Math.abs(offsetY));
                    }
                }
            }
        };


        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;
                // remove rectangle
                // note: we want to keep the ruuberband selection for the cropping => code is just commented out
                /*
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove(circle);
                */
            }
        };
        private static final class DragContext {

            public double mouseAnchorX;
            public double mouseAnchorY;
        }
    }
}
