const DEFAULT_MAX_DELTA = 32

function getGlobalRaf() {
  if (typeof window === 'undefined') {
    return null
  }
  if (typeof window.requestAnimationFrame !== 'function') {
    return null
  }
  return {
    requestAnimationFrame: window.requestAnimationFrame.bind(window),
    cancelAnimationFrame: window.cancelAnimationFrame.bind(window)
  }
}

function toFiniteNumber(value, fallback = 0) {
  const number = Number(value)
  return Number.isFinite(number) ? number : fallback
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

export function normalizeLoopOffset(offset, loopHeight) {
  const safeLoopHeight = toFiniteNumber(loopHeight, 0)
  if (safeLoopHeight <= 0) {
    return 0
  }
  const safeOffset = toFiniteNumber(offset, 0)
  return ((safeOffset % safeLoopHeight) + safeLoopHeight) % safeLoopHeight
}

export function createRafLoop(options = {}) {
  const raf = options.raf || getGlobalRaf()
  if (!raf) {
    throw new Error('createRafLoop requires requestAnimationFrame/cancelAnimationFrame in non-browser environments')
  }

  const maxDelta = toFiniteNumber(options.maxDelta, DEFAULT_MAX_DELTA)
  const onFrame = typeof options.onFrame === 'function' ? options.onFrame : null
  const shouldRun = typeof options.shouldRun === 'function' ? options.shouldRun : () => true
  let rafId = null
  let lastTimestamp = 0
  let destroyed = false

  function stop() {
    if (rafId) {
      raf.cancelAnimationFrame(rafId)
      rafId = null
    }
    lastTimestamp = 0
  }

  function step(timestamp) {
    rafId = null
    if (destroyed || !shouldRun()) {
      lastTimestamp = 0
      return
    }

    if (!lastTimestamp) {
      lastTimestamp = timestamp
    }
    const delta = clamp(timestamp - lastTimestamp, 0, maxDelta)
    lastTimestamp = timestamp

    if (onFrame) {
      onFrame({ timestamp, delta })
    }
    if (!destroyed && shouldRun()) {
      rafId = raf.requestAnimationFrame(step)
    }
  }

  return {
    start() {
      if (destroyed || rafId) {
        return
      }
      rafId = raf.requestAnimationFrame(step)
    },
    stop,
    resetTimestamp() {
      lastTimestamp = 0
    },
    destroy() {
      destroyed = true
      stop()
    },
    isRunning() {
      return Boolean(rafId)
    }
  }
}

export function createAutoScrollRuntime(options = {}) {
  const speed = toFiniteNumber(options.speed, 0)
  const getLoopHeight = typeof options.getLoopHeight === 'function'
    ? options.getLoopHeight
    : () => toFiniteNumber(options.loopHeight, 0)
  const onOffset = typeof options.onOffset === 'function' ? options.onOffset : null
  const shouldRun = typeof options.shouldRun === 'function' ? options.shouldRun : () => true
  let offset = normalizeLoopOffset(options.initialOffset, getLoopHeight())

  const loop = createRafLoop({
    raf: options.raf,
    maxDelta: options.maxDelta,
    shouldRun,
    onFrame({ delta, timestamp }) {
      const loopHeight = getLoopHeight()
      if (loopHeight <= 0 || speed <= 0) {
        offset = 0
      } else {
        offset = normalizeLoopOffset(offset + speed * delta, loopHeight)
      }
      if (onOffset) {
        onOffset({ offset, delta, timestamp, loopHeight })
      }
    }
  })

  return {
    start: loop.start,
    stop: loop.stop,
    destroy: loop.destroy,
    resetTimestamp: loop.resetTimestamp,
    isRunning: loop.isRunning,
    getOffset() {
      return offset
    },
    setOffset(nextOffset) {
      offset = normalizeLoopOffset(nextOffset, getLoopHeight())
      if (onOffset) {
        onOffset({ offset, delta: 0, timestamp: 0, loopHeight: getLoopHeight() })
      }
    },
    syncLoopHeight() {
      offset = normalizeLoopOffset(offset, getLoopHeight())
      return offset
    }
  }
}

export function getFixedVirtualWindow(options = {}) {
  const items = Array.isArray(options.items) ? options.items : []
  const itemHeight = Math.max(1, toFiniteNumber(options.itemHeight, 1))
  const viewportHeight = Math.max(0, toFiniteNumber(options.viewportHeight, 0))
  const bufferCount = Math.max(0, Math.floor(toFiniteNumber(options.bufferCount, 0)))
  const totalItems = items.length
  const totalHeight = totalItems * itemHeight
  const maxOffset = Math.max(0, totalHeight - viewportHeight)
  const virtualOffset = clamp(toFiniteNumber(options.offset, 0), 0, maxOffset)
  const visibleCount = viewportHeight > 0 ? Math.ceil(viewportHeight / itemHeight) : 0
  const baseStartIndex = Math.floor(virtualOffset / itemHeight)
  const startIndex = Math.max(0, baseStartIndex - bufferCount)
  const endIndex = Math.min(totalItems, startIndex + visibleCount + bufferCount * 2)
  const offsetY = startIndex * itemHeight
  const getKey = typeof options.getKey === 'function' ? options.getKey : null

  const visibleItems = items.slice(startIndex, endIndex).map((item, index) => {
    const virtualIndex = startIndex + index
    const key = getKey ? getKey(item, virtualIndex) : virtualIndex
    return {
      item,
      key,
      virtualIndex
    }
  })

  return {
    totalItems,
    totalHeight,
    itemHeight,
    viewportHeight,
    visibleCount,
    startIndex,
    endIndex,
    offsetY,
    virtualOffset,
    maxOffset,
    visibleItems
  }
}

export function chunkRows(rows, columns) {
  const source = Array.isArray(rows) ? rows : []
  const safeColumns = Math.max(1, Math.floor(toFiniteNumber(columns, 1)))
  const chunks = []
  for (let index = 0; index < source.length; index += safeColumns) {
    chunks.push(source.slice(index, index + safeColumns))
  }
  return chunks
}

export function padRowsWithPlaceholder(rows, columns, options = {}) {
  const placeholderFactory = typeof options.placeholderFactory === 'function'
    ? options.placeholderFactory
    : () => ({ __placeholder: true })
  const safeColumns = Math.max(1, Math.floor(toFiniteNumber(columns, 1)))
  return chunkRows(rows, safeColumns).map((row, rowIndex) => {
    if (row.length >= safeColumns) {
      return row
    }
    const padded = row.slice()
    while (padded.length < safeColumns) {
      padded.push(placeholderFactory({ rowIndex, slotIndex: padded.length }))
    }
    return padded
  })
}
