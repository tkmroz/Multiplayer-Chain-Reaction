import java.awt.*;

public class ColorUtil {

    public static Color fxToAwt(javafx.scene.paint.Color color){
        return new Color((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(), (float)color.getOpacity());
    }

    public static javafx.scene.paint.Color awtToFx(Color color){
        return new javafx.scene.paint.Color(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, color.getAlpha()/255.0);
    }
}