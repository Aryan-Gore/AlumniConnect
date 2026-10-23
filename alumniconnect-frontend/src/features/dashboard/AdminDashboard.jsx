import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Users, UserCheck, UserRoundSearch, UserRoundX, CalendarDays, Handshake, Briefcase, ScrollText } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { StatCard } from '@/components/ui/StatCard'
import { Card, CardHeader, CardTitle, CardBody } from '@/components/ui/Card'
import { adminApi } from '@/api/adminApi'
import { CHART_COLORS, tooltipContentStyle } from '@/utils/chartTheme'
import { Spinner } from '@/components/ui/Spinner'

const EMPTY_SUMMARY = {
  totalAlumni: 0, activeAlumni: 0, claimedProfiles: 0, unclaimedProfiles: 0,
  upcomingEvents: 0, activeMentorships: 0, publishedJobs: 0, pendingJobApprovals: 0,
  byDepartment: [], byGraduationYear: [],
}

export function AdminDashboard() {
  const [summary, setSummary] = useState(EMPTY_SUMMARY)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    adminApi.dashboardSummary()
      .then((data) => setSummary({ ...EMPTY_SUMMARY, ...data }))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const s = summary

  if (loading) return <div className="flex justify-center py-16"><Spinner /></div>

  return (
    <div>
      <PageHeader title="Admin overview" description="Institution-wide alumni data and engagement, at a glance." />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total alumni" value={s.totalAlumni} icon={Users} />
        <StatCard label="Active alumni" value={s.activeAlumni} icon={UserCheck} />
        <StatCard label="Claimed profiles" value={s.claimedProfiles} icon={UserRoundSearch} />
        <StatCard label="Unclaimed profiles" value={s.unclaimedProfiles} icon={UserRoundX} />
        <StatCard label="Upcoming events" value={s.upcomingEvents} icon={CalendarDays} />
        <StatCard label="Active mentorships" value={s.activeMentorships} icon={Handshake} />
        <StatCard label="Published jobs" value={s.publishedJobs} icon={Briefcase} />
        <StatCard label="Pending job approvals" value={s.pendingJobApprovals} icon={ScrollText} />
      </div>

      <div className="mt-5 grid gap-5 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Alumni by department</CardTitle>
          </CardHeader>
          <CardBody className="h-72">
            {s.byDepartment.length === 0 ? (
              <div className="flex h-full items-center justify-center text-sm text-text-tertiary">No data yet.</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={s.byDepartment} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} vertical={false} />
                  <XAxis
                    dataKey="label"
                    tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }}
                    axisLine={{ stroke: CHART_COLORS.axisLine }}
                    tickLine={false}
                  />
                  <YAxis tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip cursor={{ fill: CHART_COLORS.cursor }} contentStyle={tooltipContentStyle} labelStyle={{ color: CHART_COLORS.tooltipLabel }} />
                  <Bar dataKey="value" fill={CHART_COLORS.seriesA} radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Alumni by graduation year</CardTitle>
          </CardHeader>
          <CardBody className="h-72">
            {s.byGraduationYear.length === 0 ? (
              <div className="flex h-full items-center justify-center text-sm text-text-tertiary">No data yet.</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={s.byGraduationYear} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={CHART_COLORS.grid} vertical={false} />
                  <XAxis
                    dataKey="label"
                    tick={{ fill: CHART_COLORS.tickText, fontSize: 11, fontFamily: 'IBM Plex Mono' }}
                    axisLine={{ stroke: CHART_COLORS.axisLine }}
                    tickLine={false}
                  />
                  <YAxis tick={{ fill: CHART_COLORS.tickText, fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip cursor={{ fill: CHART_COLORS.cursor }} contentStyle={tooltipContentStyle} labelStyle={{ color: CHART_COLORS.tooltipLabel }} />
                  <Bar dataKey="value" fill={CHART_COLORS.seriesB} radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  )
}
