//import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.Sys;
import java.nio.*;  // Native IO buffers: LWJGL uses these to efficiently exchange data with system memory
import java.util.Arrays;

public class LWJGL_LightedScene {
  public static final int SIZE_FLOAT = 4;

  private static final int WINDOW_WIDTH = 800;
  private static final int WINDOW_HEIGHT = 600;
  public static final String WINDOW_TITLE = "Static Sphere";

  private static final int FRAME_RATE = 30;
  
  private static final double TXN_FACTOR = 0.3;
  private static final double RXN_FACTOR = 0.008;

  private static int polygonMode = GL_FILL;

  private static double cx = 0.0d;
  private static double cy = 2.0d;
  private static double cz = 20.0d;

  private static double crx = 0.0d;
  private static double cry = 0.0d;

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
    // Put the mouse in the center of the window, so that the user is
    // intuitively able to shift left and rotate left.
    Mouse.setCursorPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);

    // Set the projection matrix.
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluPerspective(45.0f, (float)WINDOW_WIDTH / (float)WINDOW_HEIGHT, 1.0f, 500.0f);
    glMatrixMode(GL_MODELVIEW);

    // Set clear color to black.
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Ensure correct display of polygons
    glEnable(GL_DEPTH_TEST);

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

        // Limit framerate to 30 fps
        //Display.sync(30);
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
    /*
    if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
      cx -= TXN_FACTOR * Math.sin(-crx) * Math.sin(cry + (Math.PI / 2));
      cz -= TXN_FACTOR * Math.cos(-crx);
      cy -= TXN_FACTOR * Math.sin(-crx) * Math.cos(cry + (Math.PI / 2));
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
      cx += TXN_FACTOR * Math.sin(-crx) * Math.sin(cry + (Math.PI / 2));
      cz += TXN_FACTOR * Math.cos(-crx);
      cy += TXN_FACTOR * Math.sin(-crx) * Math.cos(cry + (Math.PI / 2));
    }
    */
    if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
      cx -= TXN_FACTOR * Math.sin(-crx) * Math.sin(cry);
      cz -= TXN_FACTOR * Math.cos(-crx);
      cy -= TXN_FACTOR * Math.sin(-crx) * Math.cos(cry);
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
      cx += TXN_FACTOR * Math.sin(-crx) * Math.sin(cry);
      cz += TXN_FACTOR * Math.cos(-crx);
      cy += TXN_FACTOR * Math.sin(-crx) * Math.cos(cry);
    }
    
    System.out.println("cx: " + cx);
    System.out.println("cy: " + cy);

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
    DisplayMode dm = Display.getDisplayMode();
    int ww = dm.getWidth();
    int wh = dm.getHeight();

    double halfpi = Math.PI / 2;
    
    // mdx > 0 when moving cursor right, mdx < 0 when moving cursor left
    // mdy > 0 when moving cursor up, mdy < 0 when moving cursor down

    // Looking left is +cry, looking right is -cry
    int mdx = Mouse.getDX();
    cry += RXN_FACTOR * -mdx;

    // Looking up is +crx, looking down is -crx
    int mdy = Mouse.getDY();
    crx += RXN_FACTOR * mdy;
    // Limit range in Y-rotation to basically 90° and -90°
    // In other words, don't allow the camera to spin upside down
    // (This isn't a plane game, although that would be cool ;))
    if (mdy > 0 && crx > halfpi) crx = halfpi;
    else if (mdy < 0 && crx < -halfpi) crx = -halfpi;

    System.out.println("mdx: " + mdx);
    System.out.println("mdy: " + mdy);
    System.out.println("cry: " + cry);
    System.out.println("crx: " + crx);
  }

  private static void logic() {

  }

  private static void render() {
    // Clear the color buffer, as well as the depth buffer (also called the z-buffer).
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Reset the modelview matrix.
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    
    // glRotatef(-angle, 1f, 0f, 0f) <=> rotate game world down  = look up
    // glRotatef(+angle, 1f, 0f, 0f) <=> rotate game world up    = look down
    // glRotatef(-angle, 0f, 1f, 0f) <=> rotate game world right = look left
    // glRotatef(+angle, 0f, 1f, 0f) <=> rotate game world left  = look right
    // 
    // glTranslate(+distance, 0f, 0f) <=> translate game world right = strafe left
    // glTranslate(-distance, 0f, 0f) <=> translate game world left = strafe right
    // 
    // glTranslate(0f, +distance, 0f) <=> translate game world up = descend
    // glTranslate(0f, -distance, 0f) <=> translate game world down = ascend
    // 
    // glTranslate(0f, 0f, +distance) <=> translate game world back = walk forward
    // glTranslate(0f, 0f, -distance) <=> translate game world forward = walk backward

    // Invert translation and rotation since we're moving the world, not the camera
    double crxd = Math.toDegrees(-crx);
    double cryd = Math.toDegrees(-cry);
    glRotatef((float)crxd, 1.0f, 0.0f, 0.0f);
    glRotatef((float)cryd, 0.0f, 1.0f, 0.0f);
    glTranslatef((float)-cx, (float)-cy, (float)-cz);
    
    //glRotatef((float)Math.PI/2, 1f, 0f, 0f);
    //glTranslatef(0f, -3f, -20f);

    // Add a light.
    float[] lightPosition = {0.0f, 5.0f, 10.0f, 1.0f};
    float[] lightAmbience = {1.0f, 1.0f, 1.0f, 1.0f};   // white
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
      drawFace(
        new float[] {-1.0f, 2.0f, 1.0f},
        new float[] {-1.0f, 0.0f, 1.0f},
        new float[] {1.0f,  0.0f, 1.0f},
        new float[] {1.0f,  2.0f, 1.0f}
      );

      // Back face
      drawFace(
        new float[] {1.0f,  2.0f, -1.0f},
        new float[] {1.0f,  0.0f, -1.0f},
        new float[] {-1.0f, 0.0f, -1.0f},
        new float[] {-1.0f, 2.0f, -1.0f}
      );

      /*
      // Top face
      drawFace(
        new float[] {-1.0f, 2.0f, -1.0f},
        new float[] {-1.0f, 2.0f, 1.0f},
        new float[] {1.0f,  2.0f, 1.0f},
        new float[] {1.0f,  2.0f, -1.0f}
      );
      */

      /*
      // Bottom face
      drawFace(
        new float[] {-1.0f, 0.0f, 1.0f},
        new float[] {-1.0f, 0.0f, -1.0f},
        new float[] {1.0f,  0.0f, -1.0f},
        new float[] {1.0f,  0.0f, 1.0f}
      );
      */

      // Left face
      drawFace(
        new float[] {-1.0f, 2.0f, -1.0f},
        new float[] {-1.0f, 0.0f, -1.0f},
        new float[] {-1.0f, 0.0f, 1.0f},
        new float[] {-1.0f, 2.0f, 1.0f}
      );

      // Right face
      drawFace(
        new float[] {1.0f, 2.0f, 1.0f},
        new float[] {1.0f, 0.0f, 1.0f},
        new float[] {1.0f, 0.0f, -1.0f},
        new float[] {1.0f, 2.0f, -1.0f}
      );
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

  private static float[] calculateNormal(float[] v1, float[] v2, float[] v3) {
    // We need to calculate the cross product, since this will give us a
    // perpendicular vector, which is the definition of a normal vector.
    // Thing is, the cross product accepts two arguments, and assumes that those
    // arguments are vectors which point in a direction perpendicular to the
    // direction in which we want our normal vector to point. However, we have
    // points which make up a plane, not vectors, and we have three of them.
    //
    // To fix this, we can simply treat our points as vectors and subtract the
    // first (v1) and second (v2) point and second (v2) and third (v3) point to
    // obtain two vectors. v2 will point to v1 along the edge from v1 to v2,
    // and v3 will point to v2 along the edge from v2 to v3.
    
    float[] d1 = new float[3];
    float[] d2 = new float[3];
    
    d1[0] = v1[0] - v2[0];
    d1[1] = v1[1] - v2[1];
    d1[2] = v1[2] - v2[2];
    
    d2[0] = v2[0] - v3[0];
    d2[1] = v2[1] - v3[1];
    d2[2] = v2[2] - v3[2];

    // Now that we have two vectors, we can find their cross product.
    // If the original points of the plane were specified in a counterclockwise
    // direction, this will produce a vector which will point in the direction
    // coming out of the plane.

    float[] x = new float[3];
    x[0] = (d1[1] * d2[2]) - (d1[2] * d2[1]);
    x[1] = (d1[2] * d2[0]) - (d1[0] * d2[2]);
    x[2] = (d1[0] * d2[1]) - (d1[1] * d2[0]);

    // Finally, we have to normalize the vector we got from the cross product,
    // because OpenGL expects a vector given to glNormalf to be a unit vector.
    // We can do this by using the Pythagorean theorem to obtain the magnitude
    // of the vector, then dividing each component of the vector by the magnitude.
    
    float len = (float)Math.sqrt((x[0] * x[0]) + (x[1] * x[1]) + (x[2] * x[2]));
    float[] r = new float[3];
    r[0] = x[0] / len;
    r[1] = x[1] / len;
    r[2] = x[2] / len;

    return r;
  }
  
  private static void drawFace(float[]... vertices) {
    float[] normal = calculateNormal(vertices[0], vertices[1], vertices[2]);
    glNormal3f(normal[0], normal[1], normal[2]);
    for (float[] v : vertices) glVertex3f(v[0], v[1], v[2]);
  }
}