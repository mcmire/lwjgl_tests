//import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.Sys;
import java.lang.Float;
import java.nio.*;  // Native IO buffers: LWJGL uses these to efficiently exchange data with system memory

public class LWJGL_LightedScene {
  public static final int SIZE_FLOAT = 4;

  private static final int WINDOW_WIDTH = 800;
  private static final int WINDOW_HEIGHT = 600;
  public static final String WINDOW_TITLE = "Static Sphere";

  private static final int FRAME_RATE = 30;

  private static int polygonMode = GL_FILL;

  private static double cx = 0.0d;
  private static double cy = -2.0d;
  private static double cz = -20.0d;

  private static double crx = 0.0d; // rotation along x axis
  private static double cry = 0.0d; // rotation along y axis

  public static void main(String[] args) {
    try {
      init();
      mainLoop();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      Sys.alert(WINDOW_TITLE, "An error occurred and the game will exit.");
    } finally {
      cleanup();
    }
    System.exit(0);
  }

  private static void init() throws Exception {
    // This will throw an LWJGLException if the display mode cannot be set.
    Display.setDisplayMode(new DisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT));

    Display.setTitle(WINDOW_TITLE);

    // Sync frame (only works on windows).
    Display.setVSyncEnabled(true);

    // This will throw an LWJGLException if the display context cannot be created.
    Display.create();

    // Hide the mouse cursor from inside the window.
    Mouse.setGrabbed(true);

    // Set the projection matrix.
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(45.0f, (float)WINDOW_WIDTH / (float)WINDOW_HEIGHT, 1.0f, 500.0f);
    glMatrixMode(GL_MODELVIEW);

    // Set clear color to black.
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Ensure correct display of polygons
    // TODO: What do these do??
    glEnable(GL_CULL_FACE);
    glEnable(GL_DEPTH_TEST);
    glDepthMask(true);

    // Enable lighting
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
  }

  private static void mainLoop() {
    while (true) {
      if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Display.isCloseRequested()) {
        break;
      }

      // Always call Display.update(), all the time.
      // This displays the rendered output, and also updates the running list
      // of mouse/keyboard events.
      Display.update();

      if (Display.isActive()) {
        // Window is active (i.e. in the foreground / currently selected),
        // so we can go full steam.
        handleKeyboard();
        handleMouse();
        logic();
        render();
        // Limit the framerate.
        //Display.sync(FRAMERATE);
      }
      else 
      {
        // Window is not active, so let's go light on the CPU.
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // do nothing
        }
        logic();

        // Only bother rendering if the window has actually changed.
        if (Display.isVisible() || Display.isDirty()) {
          render();
        }
      }
    }
  }

  private static void handleKeyboard() {
    if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
      cx += Math.sin(-cry + (Math.PI / 2));
      cz += Math.cos(-cry + (Math.PI / 2));
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
      cx -= Math.sin(-cry + (Math.PI / 2));
      cz -= Math.cos(-cry + (Math.PI / 2));
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
      cx += Math.sin(-cry);
      cz += Math.cos(-cry);
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
      cx -= Math.sin(-cry);
      cz -= Math.cos(-cry);
    }

    while (Keyboard.next()) {
      boolean keyDownEvent = Keyboard.getEventKeyState();
      int eventKey = Keyboard.getEventKey();
      if (!keyDownEvent) { // key up event
        if (eventKey == Keyboard.KEY_F2) {
          if (polygonMode == GL_LINE) polygonMode = GL_FILL;
          else                        polygonMode = GL_LINE;

          glPolygonMode(GL_FRONT, polygonMode);
        }
      }
    }

    /*
    while (Keyboard.next()) {
      boolean keyDownEvent = Keyboard.getEventKeyState();
      int eventKey = Keyboard.getEventKey();
      System.out.println((keyDownEvent ? "key down: " : "key up: ") + Keyboard.getKeyName(eventKey));
    }
    */
  }

  private static void handleMouse() {
    // Don't bother setting the camera if the window hasn't been put inside the window yet
    if (!Mouse.isInsideWindow()) return;

    // mouseX: 0..windowWidth -> -pi..pi deg
    // mouseY: 0..windowHeight -> pi/2..-pi/2 deg

    DisplayMode dm = Display.getDisplayMode();
    int ww = dm.getWidth();
    int wh = dm.getHeight();

    // And because I fail math: Mapping x from (a..b) to (c..d) = x' = ((x-a)(d-c)/(b-a))+c
    // since a is 0, that simplifies to: (x(d-c)/b)+c
    // <https://groups.google.com/forum/#!topic/alt.math/sj4tTuXpxE0>
    int mx = Mouse.getX();
    cry = ((2 * Math.PI * mx) / ww) - Math.PI;
    //cry = -cry;

    int my = Mouse.getY();
    crx = ((Math.PI * my) / wh) - (Math.PI / 2);
    crx = -crx;
  }

  private static void logic() {

  }

  private static void render() {
    // Clear the color buffer, as well as the depth buffer (also called the z-buffer).
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Reset the modelview matrix.
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    double crxd = Math.toDegrees(crx);
    double cryd = Math.toDegrees(cry);
    glRotatef((float)crxd, 1.0f, 0.0f, 0.0f);
    glRotatef((float)cryd, 0.0f, 1.0f, 0.0f);
    glTranslatef((float)cx, (float)cy, (float)cz);

    // Add a light.
    float lightPosition[] = {0.0f, 5.0f, 10.0f, 1.0f};
    float lightAmbience[] = {1.0f, 1.0f, 1.0f, 1.0f};   // white
    // <-- default values for diffuse and specular are 1.0f, 1.0f, 1.0f, 1.0f
    glLight(GL_LIGHT0, GL_POSITION, allocFloats(lightPosition));
    glLight(GL_LIGHT0, GL_AMBIENT, allocFloats(lightAmbience));

    // Set the color to white.
    glColor3f(1.0f, 1.0f, 1.0f);

    // Draw a floor.
    glBegin(GL_QUADS);
      glVertex3f(-30.0f, -0.5f, -30.0f);
      glVertex3f(-30.0f, -0.5f, 30.0f);
      glVertex3f(30.0f,  -0.5f, 30.0f);
      glVertex3f(30.0f,  -0.5f, -30.0f);
    glEnd();

    // Set the color to a light blue.
    glColor3f(0.79f, 0.45f, 1.0f);

    // Draw a cube in the middle of the floor.
    glBegin(GL_QUADS);
      // Front face
      glVertex3f(-1.0f, 2.0f, 0.0f);
      glVertex3f(-1.0f, 0.0f, 0.0f);
      glVertex3f(1.0f,  0.0f, 0.0f);
      glVertex3f(1.0f,  2.0f, 0.0f);

      // Back face
      glVertex3f(-1.0f, 2.0f, 1.0f);
      glVertex3f(-1.0f, 0.0f, 1.0f);
      glVertex3f(1.0f,  0.0f, 1.0f);
      glVertex3f(1.0f,  2.0f, 1.0f);

      // Top face
      glVertex3f(-1.0f, 2.0f, 0.0f);
      glVertex3f(-1.0f, 2.0f, 1.0f);
      glVertex3f(1.0f,  2.0f, 1.0f);
      glVertex3f(1.0f,  2.0f, 0.0f);

      // Bottom face
      glVertex3f(-1.0f, 0.0f, 0.0f);
      glVertex3f(-1.0f, 0.0f, 1.0f);
      glVertex3f(1.0f,  0.0f, 1.0f);
      glVertex3f(1.0f,  0.0f, 0.0f);

      // Left face
      glVertex3f(-1.0f, 2.0f, 0.0f);
      glVertex3f(-1.0f, 0.0f, 0.0f);
      glVertex3f(-1.0f, 0.0f, 1.0f);
      glVertex3f(-1.0f, 2.0f, 1.0f);

      // Right face
      glVertex3f(1.0f, 2.0f, 0.0f);
      glVertex3f(1.0f, 0.0f, 0.0f);
      glVertex3f(1.0f, 0.0f, 1.0f);
      glVertex3f(1.0f, 2.0f, 1.0f);
    glEnd();
  }

  private static void cleanup() {
    Display.destroy();
  } 

  private static FloatBuffer allocFloats(float[] floatarray) {
    FloatBuffer fb = ByteBuffer.allocateDirect(floatarray.length * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
    fb.put(floatarray).flip();
    return fb;
  }
}