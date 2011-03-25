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

  private static double[] cpos = new double[] { 0.0d, 2.0d, 20.0d };
  private static double[] crot = new double[] { 0.0d, 0.0d };
  
  private static double[] DEFAULT_MOVEMENT = new double[] { 0, 0, -0.5 };

  private static double[] IDENTITY_MATRIX = new double[] {
    1, 0, 0,
    0, 1, 0,
    0, 0, 1
  };

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
      System.out.println(" ");
      
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
    // Calculate lookat vector from the two rotations using matrix multiplication.
    //double[] lookat = mMulti(mMulti(DEFAULT_MOVEMENT, rotXMatrix(crot[0])), rotYMatrix(crot[1]));
    double[] lookat = mMulti(DEFAULT_MOVEMENT, rotXYMatrix(crot[0], crot[1]));
    
    System.out.println("lookat: (" + lookat[0] + ", " + lookat[1] + ", " + lookat[2] + ")");
    
    // The left vector is always perpendicular to the Y axis.
    // If the camera is pointed upward, for instance (i.e. the rotX value of the
    // lookat vector is > 0), we don't want the left vector to be perpendicular
    // to the lookat vector relative to an up vector of (0, 1, 0), because that
    // would mean the left vector would basically rotate along the Z axis
    // (from (1, 0, 0)), which is bad because we don't give a way to recover 
    // from a roll rotation.
    //
    // Imagine that the camera is housed inside a sphere. The sphere can rotate
    // upward and downward (which will affect its height moving forward and
    // backward), but this does not change the fact that when the sphere moves
    // left or right it does so perpendicular to the Y axis.
    //
    double[] left = mMulti(DEFAULT_MOVEMENT, rotYMatrix(crot[1] + (Math.PI / 2)));
    
    System.out.println("left: (" + left[0] + ", " + left[1] + ", " + left[2] + ")");
    
    if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
      cpos = vAdd(cpos, left);
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
      cpos = vSubtract(cpos, left);
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
      cpos = vAdd(cpos, lookat);
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
      cpos = vSubtract(cpos, lookat);
    }
    
    System.out.println("cpos: (" + cpos[0] + ", " + cpos[1] + ", " + cpos[2] + ")");

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
    
    int[] md = new int[2];
    
    // mdx > 0 when moving cursor right, mdx < 0 when moving cursor left
    // mdy > 0 when moving cursor up, mdy < 0 when moving cursor down

    // Looking left is +crot[1], looking right is -crot[1]
    md[0] = Mouse.getDX();
    crot[1] += RXN_FACTOR * -md[0];

    // Looking up is +crot[0], looking down is -crot[0]
    md[1] = Mouse.getDY();
    crot[0] += RXN_FACTOR * md[1];
    // Limit range in Y-rotation to basically 90° and -90°
    // In other words, don't allow the camera to spin upside down
    // (This isn't a plane game, although that would be cool ;))
    if (md[1] > 0 && crot[0] > halfpi) crot[0] = halfpi;
    else if (md[1] < 0 && crot[0] < -halfpi) crot[0] = -halfpi;

    System.out.println("md: (" + md[0] + ", " + md[1] + ")");
    System.out.println("crot: (" + crot[0] + ", " + crot[1] + ")");
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
    double[] crotd = new double[] {
      Math.toDegrees(crot[0]),
      Math.toDegrees(crot[1])
    };
    glRotatef((float)-crotd[0], 1.0f, 0.0f, 0.0f);
    glRotatef((float)-crotd[1], 0.0f, 1.0f, 0.0f);
    glTranslatef((float)-cpos[0], (float)-cpos[1], (float)-cpos[2]);
    
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
    
    float[] r = vNormalize(x);

    return r;
  }
  
  private static void drawFace(float[]... vertices) {
    float[] normal = calculateNormal(vertices[0], vertices[1], vertices[2]);
    glNormal3f(normal[0], normal[1], normal[2]);
    for (float[] v : vertices) glVertex3f(v[0], v[1], v[2]);
  }
  
  public static double[] rotXMatrix(double rotx) {
    double[] mat = new double[9];
    mat[0] = 1;
    mat[1] = 0;
    mat[2] = 0;
    mat[3] = 0;
    mat[4] = Math.cos(rotx);
    mat[5] = -Math.sin(rotx);
    mat[6] = 0;
    mat[7] = Math.sin(rotx);
    mat[8] = Math.cos(rotx);
    return mat;
  }
  
  public static double[] rotYMatrix(double roty) {
    double[] mat = new double[9];
    mat[0] = Math.cos(roty);
    mat[1] = 0;
    mat[2] = Math.sin(roty);
    mat[3] = 0;
    mat[4] = 1;
    mat[5] = 0;
    mat[6] = -Math.sin(roty);
    mat[7] = 0;
    mat[8] = Math.cos(roty);
    return mat;
  }
  
  public static double[] rotZMatrix(double rotz) {
    double[] mat = new double[9];
    mat[0] = Math.cos(rotz);
    mat[1] = -Math.sin(rotz);
    mat[2] = 0;
    mat[3] = Math.sin(rotz);
    mat[4] = Math.cos(rotz);
    mat[5] = 0;
    mat[6] = 0;
    mat[7] = 0;
    mat[8] = 1;
    return mat;
  }
  
  public static double[] rotXYMatrix(double rotx, double roty) {
    double[] mat = new double[9];
    mat[0] = Math.cos(roty);
    mat[1] = 0;
    mat[2] = Math.sin(roty);
    mat[3] = Math.sin(rotx) * Math.sin(roty);
    mat[4] = Math.cos(rotx);
    mat[5] = -Math.sin(rotx) * Math.cos(roty);
    mat[6] = Math.cos(rotx) * -Math.sin(roty);
    mat[7] = Math.sin(rotx);
    mat[8] = Math.cos(rotx) * Math.cos(roty);
    return mat;
  }
  
  public static double[] mMulti(double[] vector, double[] matrix) {  
    double[] matrix2 = new double[3];
    matrix2[0] = matrix[0] * vector[0] +
                 matrix[1] * vector[1] +
                 matrix[2] * vector[2];
    matrix2[1] = matrix[3] * vector[0] +
                 matrix[4] * vector[1] +
                 matrix[5] * vector[2];
    matrix2[2] = matrix[6] * vector[0] +
                 matrix[7] * vector[1] +
                 matrix[8] * vector[2];
    return matrix2;
  }
  
  public static float[] vAdd(float[] v1, float[] v2) {
    return new float[] {
      v1[0] + v2[0],
      v1[1] + v2[1],
      v1[2] + v2[2]
    };
  }
  public static double[] vAdd(double[] v1, double[] v2) {
    return new double[] {
      v1[0] + v2[0],
      v1[1] + v2[1],
      v1[2] + v2[2]
    };
  }
  
  public static float[] vSubtract(float[] v1, float[] v2) {
    return new float[] {
      v1[0] - v2[0],
      v1[1] - v2[1],
      v1[2] - v2[2]
    };
  }
  public static double[] vSubtract(double[] v1, double[] v2) {
    return new double[] {
      v1[0] - v2[0],
      v1[1] - v2[1],
      v1[2] - v2[2]
    };
  }
  
  public static float[] vNormalize(float[] v) {
    float len = (float)Math.sqrt((v[0] * v[0]) + (v[1] * v[1]) + (v[2] * v[2]));
    return new float[] {
      v[0] / len,
      v[1] / len,
      v[2] / len
    };
  }
  public static double[] vNormalize(double[] v) {
    double len = Math.sqrt((v[0] * v[0]) + (v[1] * v[1]) + (v[2] * v[2]));
    return new double[] {
      v[0] / len,
      v[1] / len,
      v[2] / len
    };
  }
}