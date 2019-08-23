package com.packtpub.libgdx.modeltest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class MyModelTest extends ApplicationAdapter {
	public Environment environment;
	public PerspectiveCamera cam;
	public CameraInputController camController;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public AssetManager assets;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Array<BoundingBox> boundingBoxes=new Array<BoundingBox>();
	public OrthographicCamera orthoCam;
	public SpriteBatch spriteBatch;
	public BitmapFont font;
	public StringBuilder stringBuilder = new StringBuilder();
	private Vector3 position = new Vector3();

	@Override
	public void create() {
		environment = new Environment();
		environment.set(new
				ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f,
						0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f,
				0.8f, -1f, -0.8f, -0.2f));
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		cam.position.set(5, 20, 20);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();
		assets = new AssetManager();
		assets.load("car.g3dj", Model.class);
		assets.finishLoading();
		model = assets.get("car.g3dj", Model.class);
		for (float x = -50; x <= 50f; x += 20) {
			for (float z = -50f; z <= 50f; z += 10f) {
				ModelInstance instance = new ModelInstance(model);
				instance.transform.setToTranslation(x, 0, z);
				// on calcule la boundingbox au moment de la creation (best practice)
				BoundingBox box = instance.calculateBoundingBox(new BoundingBox());
				instances.add(instance);
				boundingBoxes.add(box);
			}
		}

		final BoundingBox box= model.calculateBoundingBox(new
				BoundingBox());
		camController = new CameraInputController(cam) {
			private final Vector3 position = new Vector3();
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int
					button) {
				Ray ray = cam.getPickRay(screenX, screenY);
				for (int i = 0; i < instances.size; i++) {
					ModelInstance instance = instances.get(i);
					instance.transform.getTranslation(position);
					Vector3 dimensions=new Vector3();
					box.getDimensions(dimensions);
					if (Intersector.intersectRayBoundsFast(ray, position, dimensions)) {
						instances.removeIndex(i);
						i--;
					}
				}
				return super.touchUp(screenX, screenY, pointer, button);
			}
		};
		Gdx.input.setInputProcessor(camController);
		
		orthoCam = new OrthographicCamera(Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		orthoCam.position.set(Gdx.graphics.getWidth() / 2f,
				Gdx.graphics.getHeight() / 2f, 0);
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
	}

	@Override
	public void render() {
		camController.update();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT |
				GL20.GL_DEPTH_BUFFER_BIT);
		modelBatch.begin(cam);
		int count = 0;
		for (int i=0; i<instances.size; ++i) {
			ModelInstance instance=instances.get(i);
			if (isVisible(cam, instance, boundingBoxes.get(i))) {
				modelBatch.render(instance, environment);
				count++;
			}
		}
		modelBatch.end();
		orthoCam.update();
		spriteBatch.setProjectionMatrix(orthoCam.combined);
		spriteBatch.begin();
		stringBuilder.setLength(0);
		stringBuilder.append("FPS: " +
				Gdx.graphics.getFramesPerSecond()).append("\n") ;
		stringBuilder.append("Cars: " + count).append("\n");
		stringBuilder.append("Total: " + instances.size).append("\n");
		font.draw(spriteBatch, stringBuilder, 0, Gdx.graphics.getHeight());

		spriteBatch.end();		
	}

	private boolean isVisible(final Camera cam, final ModelInstance instance, final BoundingBox box) {
		instance.transform.getTranslation(position);
		Vector3 dimensions=new Vector3();
		box.getDimensions(dimensions);
		return cam.frustum.boundsInFrustum(position, dimensions );
	}

	// calcul a l'affichage de la boundingbox (a ne pas faire)
	private boolean isVisible(PerspectiveCamera cam, ModelInstance instance) {
		instance.transform.getTranslation(position);
		BoundingBox box = instance.calculateBoundingBox(new BoundingBox());
		Vector3 dimensions=new Vector3();
		box.getDimensions(dimensions);
		return cam.frustum.boundsInFrustum(position, dimensions);
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		assets.dispose();
	}
}