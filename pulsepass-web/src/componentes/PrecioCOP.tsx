import { formatearCOP } from '@/lib/formato'

export default function PrecioCOP({ valor }: { valor: number }) {
  return <span className="tabular-nums">{formatearCOP(valor)}</span>
}
