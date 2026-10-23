// Shared Recharts color values — SVG fill/stroke need literal strings, so
// this mirrors the palette in styles/tokens.css. Update both together.
export const CHART_COLORS = {
  grid: '#ebedf0',
  axisLine: '#dbdfe3',
  tickText: '#5e6b78',
  tooltipBg: '#ffffff',
  tooltipBorder: '#dbdfe3',
  tooltipLabel: '#1b1f23',
  cursor: 'rgba(10, 102, 194, 0.06)',
  seriesA: '#0a66c2',
  seriesB: '#4b7ba6',
  seriesC: '#057642',
}

export const tooltipContentStyle = {
  background: CHART_COLORS.tooltipBg,
  border: `1px solid ${CHART_COLORS.tooltipBorder}`,
  borderRadius: 8,
  fontSize: 12,
  boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
}
