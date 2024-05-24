package com.tlh.demo1


import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder


class MyWatchFaceService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {

    }

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine : CanvasWatchFaceService.Engine() {

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            // Draw a black background.
            canvas.drawColor(Color.BLACK)
        }
    }
}