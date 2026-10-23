import { cn } from '@/utils/cn'

/*
  Plain table primitives so every admin list looks and behaves the same.

    <TableWrap>
      <Table>
        <THead><Tr><Th>Name</Th><Th className="text-right">Actions</Th></Tr></THead>
        <TBody>{rows.map((row) => <Tr key={row.id}><Td>{row.name}</Td><Td className="text-right">…</Td></Tr>)}</TBody>
      </Table>
    </TableWrap>

  TableWrap scrolls sideways on a phone instead of breaking the page layout.
*/

export function TableWrap({ className, children }) {
  return <div className={cn('overflow-x-auto rounded-lg border border-border bg-surface', className)}>{children}</div>
}

export function Table({ className, children, ...props }) {
  return (
    <table className={cn('w-full min-w-[40rem] border-collapse text-left text-sm', className)} {...props}>
      {children}
    </table>
  )
}

export function THead({ className, children, ...props }) {
  return (
    <thead className={cn('border-b border-border bg-surface-sunken', className)} {...props}>
      {children}
    </thead>
  )
}

export function TBody({ className, children, ...props }) {
  return (
    <tbody className={cn('divide-y divide-border-soft', className)} {...props}>
      {children}
    </tbody>
  )
}

export function Tr({ className, children, ...props }) {
  return (
    <tr className={cn('hover:bg-surface-sunken/60', className)} {...props}>
      {children}
    </tr>
  )
}

export function Th({ className, children, ...props }) {
  return (
    <th
      scope="col"
      className={cn('whitespace-nowrap px-4 py-2.5 text-xs font-medium uppercase tracking-wider text-text-tertiary', className)}
      {...props}
    >
      {children}
    </th>
  )
}

export function Td({ className, children, ...props }) {
  return (
    <td className={cn('px-4 py-3 align-middle text-text-primary', className)} {...props}>
      {children}
    </td>
  )
}
