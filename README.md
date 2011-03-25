Hi! This is a tiny repo where I am experimenting with LWJGL as I learn it.

All the source files in are src/. The *Example.java files are copied straight from <http://ninjacave.com/tutorials>, but the one I started from scratch and am working on is LWJGL_LightedScene.java. LWJGL comes bundled with the project, so to run this file, all you should have to say is:

    ant jar run -Dclassname=LWJGL_LightedScene
    
You should be looking at a cube. It's a standard FPS view, so you can move your mouse to look around, and WASD will take you where you want to go.