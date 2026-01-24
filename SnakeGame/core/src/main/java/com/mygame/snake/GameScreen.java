/*
 * Copyright (C) 2026  bo7dan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mygame.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;

public class GameScreen extends ScreenAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private static final float MOVE_TIME = 0.12f;
    private float timer = MOVE_TIME;

    private static final int GRID_SIZE = 20;
    private final Array<SnakeElement> snakeBody = new Array<>();
    private int appleX, appleY;
    private int score = 0;

    // Стан гри: true - граємо, false - програли
    private boolean alive = true;

    private int direction = 3;
    private boolean directionChanged = false;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont(); // Стандартний шрифт LibGDX
        font.getData().setScale(2); // Робимо текст більшим

        resetGame();
    }

    private void resetGame() {
        snakeBody.clear();
        snakeBody.add(new SnakeElement(5, 5));
        snakeBody.add(new SnakeElement(4, 5));
        snakeBody.add(new SnakeElement(3, 5));
        score = 0;
        direction = 3;
        alive = true;
        spawnApple();
    }

    private void spawnApple() {
        appleX = MathUtils.random((Gdx.graphics.getWidth() / GRID_SIZE) - 1);
        appleY = MathUtils.random((Gdx.graphics.getHeight() / GRID_SIZE) - 1);
    }

    @Override
    public void render(float delta) {
        if (alive) {
            queryInput();
            timer -= delta;
            if (timer <= 0) {
                timer = MOVE_TIME;
                moveSnake();
            }
        } else {
            // Якщо програли, чекаємо натискання Пробілу для рестарту
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                resetGame();
            }
        }

        draw();
    }

    private void queryInput() {
        if (!directionChanged) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP) && direction != 1) { direction = 0; directionChanged = true; }
            else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && direction != 0) { direction = 1; directionChanged = true; }
            else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && direction != 3) { direction = 2; directionChanged = true; }
            else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && direction != 2) { direction = 3; directionChanged = true; }
        }
    }

    private void moveSnake() {
        SnakeElement head = snakeBody.first();
        int newX = head.x;
        int newY = head.y;

        if (direction == 0) newY++;
        if (direction == 1) newY--;
        if (direction == 2) newX--;
        if (direction == 3) newX++;

        if (newX == appleX && newY == appleY) {
            score += 10; // Додаємо очки за яблуко
            spawnApple();
        } else {
            snakeBody.removeIndex(snakeBody.size - 1);
        }

        int gridWidth = Gdx.graphics.getWidth() / GRID_SIZE;
        int gridHeight = Gdx.graphics.getHeight() / GRID_SIZE;

        if (newX < 0) newX = gridWidth - 1;
        else if (newX >= gridWidth) newX = 0;
        if (newY < 0) newY = gridHeight - 1;
        else if (newY >= gridHeight) newY = 0;

        for (SnakeElement se : snakeBody) {
            if (se.x == newX && se.y == newY) {
                alive = false; // Врізалися в себе — Game Over
                return;
            }
        }

        snakeBody.insert(0, new SnakeElement(newX, newY));
        directionChanged = false;
    }

    private void draw() {
        Gdx.gl.glClearColor(0.4f, 0.5f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Малюємо змійку та яблуко через ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        for (SnakeElement el : snakeBody) {
            shapeRenderer.rect(el.x * GRID_SIZE, el.y * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
        }
        shapeRenderer.rect(appleX * GRID_SIZE, appleY * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
        shapeRenderer.end();

        // 2. Малюємо текст через SpriteBatch
        batch.begin();
        font.setColor(0, 0, 0, 1); // Чорний текст

        // Малюємо рахунок у лівому верхньому куті
        font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);

        if (!alive) {
            // Виводимо напис Game Over по центру
            font.draw(batch, "GAME OVER!", Gdx.graphics.getWidth() / 2f - 70, Gdx.graphics.getHeight() / 2f + 20);
            font.draw(batch, "Press SPACE to Restart", Gdx.graphics.getWidth() / 2f - 130, Gdx.graphics.getHeight() / 2f - 20);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
