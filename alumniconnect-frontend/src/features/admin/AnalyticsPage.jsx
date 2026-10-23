import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { PageHeader } from '@/components/common/PageHeader'
import { Card, CardHeader, CardTitle, CardBody } from '@/components/ui/Card'
import { adminApi } from '@/api/adminApi'
import { CHART_COLORS, tooltipContentStyle } from '@/utils/chartTheme'
import { Spinner } from '@/components/ui/Spinner'

function EmptyChart() {
  return <div className="flex h-full items-center justify-center text-sm text-text-tertiary">No data yet.</div>
}

export function AnalyticsPage() {
  const [byIndustry, setByIndustry] = useState([])
  const [byLocation, setByLocation] = useState([])
  const [byDepartment, setByDepartment] = useState([])
  const [byBatch, setByBatch] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      adminApi.alumniByIndustry().catch(() => []),
      adminApi.alumniByLocation().catch(() => []),
      adminApi.alumniByDepartment().catch(() => []),
      adminApi.alumniByBatch().catch(() => []),
    ]).then(([industry, location, dept, batch]) => {
      setByIndustry(industry ?? [])
      setByLocation(location ?? [])
      setByDepartment(dept ?? [])
      setByBatch(batch ?? [])
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader title="Analytics" description="Deeper cuts of alumni and engagement data, computed by the Python/Pandas job." />

      <div className="grid gap-5 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Alumni by industry</CardTitle></CardHeader>
          <CardBody className="h-72">
            {byIndustry.length === 0 ? <EmptyChart /> : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={byIndustry} layout="vertical" margin={{ top: 8, right: 16, left: 8, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} horizontal={false} />
                  <XAxis type="number" tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis
                    type="category" dataKey="label" width={90}
                    tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }}
                    axisLine={{ stroke: CHART_COLORS.axisLine }} tickLine={false}
                  />
                  <Tooltip cursor={{ fill: CHART_COLORS.cursor }} contentStyle={tooltipContentStyle} labelStyle={{ color: CHART_COLORS.tooltipLabel }} />
                  <Bar dataKey="value" fill={CHART_COLORS.seriesA} radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader><CardTitle>Alumni by location</CardTitle></CardHeader>
          <CardBody className="h-72">
            {byLocation.length === 0 ? <EmptyChart /> : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={byLocation} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} vertical={false} />
                  <XAxis dataKey="label" tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={{ stroke: CHART_COLORS.axisLine }} tickLine={false} />
                  <YAxis tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip cursor={{ fill: CHART_COLORS.cursor }} contentStyle={tooltipContentStyle} labelStyle={{ color: CHART_COLORS.tooltipLabel }} />
                  <Bar dataKey="value" fill={CHART_COLORS.seriesC} radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader><CardTitle>Alumni by department</CardTitle></CardHeader>
          <CardBody className="h-72">
            {byDepartment.length === 0 ? <EmptyChart /> : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={byDepartment} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} vertical={false} />
                  <XAxis dataKey="label" tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={{ stroke: CHART_COLORS.axisLine }} tickLine={false} />
                  <YAxis tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip cursor={{ fill: CHART_COLORS.cursor }} contentStyle={tooltipContentStyle} labelStyle={{ color: CHART_COLORS.tooltipLabel }} />
                  <Bar dataKey="value" fill={CHART_COLORS.seriesB} radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader><CardTitle>Alumni by graduation batch</CardTitle></CardHeader>
          <CardBody className="h-72">
            {byBatch.length === 0 ? <EmptyChart /> : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={byBatch} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} vertical={false} />
                  <XAxis dataKey="label" tick={{ fill: CHART_COLORS.tickText, fontSize: 11, fontFamily: 'IBM Plex Mono' }} axisLine={{ stroke: CHART_COLORS.axisLine }} tickLine={false} />
                  <YAxis tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip cursor={{ fill: CHART_COLORS.cursor }} contentStyle={tooltipContentStyle} labelStyle={{ color: CHART_COLORS.tooltipLabel }} />
                  <Bar dataKey="value" fill={CHART_COLORS.seriesA} radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  )
}
