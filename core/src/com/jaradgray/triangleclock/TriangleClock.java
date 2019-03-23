package com.jaradgray.triangleclock;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TriangleClock extends ApplicationAdapter {

	// constants
	private static final String LOG_TAG = "logtag";
	public static final float DEG_PER_SEC = 6f;
	public static final float DEG_PER_MIN = 360f / 60f;
	public static final float DEG_PER_HOUR = 360f / 12f;
	public static final float DEG_PER_SEC_SECOND = DEG_PER_SEC;				// degrees the second hand moves each second
	public static final float DEG_PER_SEC_MINUTE = DEG_PER_MIN / 60f;		// degrees the minute hand moves each second
	public static final float DEG_PER_SEC_HOUR = DEG_PER_HOUR / 60f / 60f;	// degrees the hour hand moves each second

	private static final Color CLEAR_COLOR = new Color(0.16f, 0.16f, 0.16f, 1);
	private static final Color TICK_COLOR = new Color(0.4f, 0.4f, 0.4f, 1);
	private static final Color CIRCLE_STROKE_COLOR = new Color(0.88f, 0.88f, 0.88f, 1);


	// instance variables
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera camera;

	private int screenSize;
	private float dialMargin;
	private float dialRadius, triangleRadius;
	private float hourCircleRadius, minuteCircleRadius, secondCircleRadius;
	private float hourTickWidth, hourTickStrokeWidth, minuteTickWidth, minuteTickStrokeWidth;
	private float hourTickRadius, minuteTickRadius;
	private float hourTickStrokeRadius, minuteTickStrokeRadius;
	private float circleStrokeThickness;
	private float hourAngle, minuteAngle, secondAngle;

	private Color themeColor = new Color(1, 1, 1, 1);

	@Override
	public void create () {
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();

		// set size to shortest of width or height
		screenSize = (h < w) ? h : w;

		// assign values based on screenSize
		dialMargin = screenSize / 20f;
		dialRadius = (screenSize / 2) - dialMargin;
		triangleRadius = dialRadius - (dialRadius * 0.25f);

		hourCircleRadius = dialRadius * 0.1f;
		minuteCircleRadius = hourCircleRadius * 0.5f;
		secondCircleRadius = minuteCircleRadius * 0.5f;

		hourTickWidth = screenSize / 50f;
		hourTickStrokeWidth = hourTickWidth * 2f;
		minuteTickWidth = hourTickWidth / 4f;
		minuteTickStrokeWidth = minuteTickWidth * 2f;

		hourTickRadius = hourCircleRadius / 2f;
		hourTickStrokeRadius = hourTickRadius * 1.2f;
		minuteTickRadius = hourTickRadius / 4f;
		minuteTickStrokeRadius = minuteTickRadius * 1.2f;

		circleStrokeThickness = (hourTickStrokeWidth - hourTickWidth) / 2f;

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapeRenderer = new ShapeRenderer();

		syncAnglesToCurrentTime();
	}

	@Override
	public void resume() {
		syncAnglesToCurrentTime();
	}

	@Override
	public void render () {
		// logic
		float delta = Gdx.graphics.getDeltaTime();
		// update secondAngle, minuteAngle, and hourAngle
		secondAngle -= DEG_PER_SEC_SECOND * delta;
		minuteAngle -= DEG_PER_SEC_MINUTE * delta;
		hourAngle -= DEG_PER_SEC_HOUR * delta;
		if (secondAngle < 0) secondAngle += 360f;
		if (minuteAngle < 0) minuteAngle += 360f;
		if (hourAngle < 0) hourAngle += 360f;

		// create points for "hands"
		Vector2 p1, p2, p3;
		p1 = getPointOnCircle(triangleRadius, hourAngle);
		p2 = getPointOnCircle(triangleRadius, minuteAngle);
		p3 = getPointOnCircle(triangleRadius, secondAngle);

		// drawing
		Gdx.gl.glClearColor(CLEAR_COLOR.r, CLEAR_COLOR.g, CLEAR_COLOR.b, CLEAR_COLOR.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// draw triangle shapes
		shapeRenderer.setProjectionMatrix(camera.combined); // effectively center viewport on camera origin
		shapeRenderer.begin(ShapeType.Filled);
		// triangle
		shapeRenderer.setColor(themeColor);
		shapeRenderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
		// hour circle
		shapeRenderer.setColor(CIRCLE_STROKE_COLOR);
		shapeRenderer.circle(p1.x, p1.y, hourCircleRadius + circleStrokeThickness);
		shapeRenderer.setColor(CLEAR_COLOR);
		shapeRenderer.circle(p1.x, p1.y, hourCircleRadius);
		// minute circle
		shapeRenderer.setColor(CIRCLE_STROKE_COLOR);
		shapeRenderer.circle(p2.x, p2.y, minuteCircleRadius + circleStrokeThickness);
		shapeRenderer.setColor(CLEAR_COLOR);
		shapeRenderer.circle(p2.x, p2.y, minuteCircleRadius);
		// second circle
		shapeRenderer.setColor(CIRCLE_STROKE_COLOR);
		shapeRenderer.circle(p3.x, p3.y, secondCircleRadius + circleStrokeThickness);
		shapeRenderer.setColor(CLEAR_COLOR);
		shapeRenderer.circle(p3.x, p3.y, secondCircleRadius);

		// minute markers
		for (int i = 0; i < 60; i++) {
			float theta = i * DEG_PER_MIN;
			Vector2 p = getPointOnCircle(dialRadius, theta);
			shapeRenderer.setColor(CLEAR_COLOR);
			shapeRenderer.circle(p.x, p.y, minuteTickStrokeRadius);
			shapeRenderer.setColor(TICK_COLOR);
			shapeRenderer.circle(p.x, p.y, minuteTickRadius);
		}

		// hour markers
		for (int i = 0; i < 12; i++) {
			float theta = i * DEG_PER_HOUR;
			Vector2 p = getPointOnCircle(dialRadius, theta);
			shapeRenderer.setColor(CLEAR_COLOR);
			shapeRenderer.circle(p.x, p.y, hourTickStrokeRadius);
			shapeRenderer.setColor(1, 1, 1, 1);
			shapeRenderer.circle(p.x, p.y, hourTickRadius);
		}

		shapeRenderer.end();
	}

	@Override
	public void dispose () {
		// nothing to dispose
	}


	// Private Methods

	/**
	 * Assigns values to secondAngle, minuteAngle, and hourAngle based on current time.
	 */
	private void syncAnglesToCurrentTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String timeString = sdf.format(cal.getTime());

		int hr, min, sec;
		hr = Integer.parseInt(timeString.substring(0, 2));
		min = Integer.parseInt(timeString.substring(3, 5));
		sec = Integer.parseInt(timeString.substring(6, 8));
		Gdx.app.log(LOG_TAG, "current time: " + timeString);

		// convert hr from 24-hour scale to 12-hour scale
		hr = hr % 12;

		// set angles
		secondAngle = -(DEG_PER_SEC_SECOND * sec);
		minuteAngle = -(DEG_PER_MIN * min) - (DEG_PER_SEC_MINUTE * sec);
		hourAngle = -(DEG_PER_HOUR * hr) - (DEG_PER_SEC_HOUR * min * 60f) - (DEG_PER_SEC_HOUR * sec);
	}

	/**
	 * Given a radius and an angle (in degrees), uses the parametric equations for a circle
	 * to return a point on the circle with origin (0, 0) and radius radiusIn at the angle
	 * theta (relative to the positive y-axis), as a Vector2.
	 *
	 * @param radiusIn
	 * @param theta
	 * @return
	 */
	private Vector2 getPointOnCircle(float radiusIn, float theta) {
		theta += 90f; // 0 deg == 12 o'clock
		float x = radiusIn * (float) MathUtils.cosDeg(theta);
		float y = radiusIn * (float) MathUtils.sinDeg(theta);
		return new Vector2(x, y);
	}
}
