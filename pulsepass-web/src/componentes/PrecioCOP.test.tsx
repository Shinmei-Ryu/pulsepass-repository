import { render, screen } from '@testing-library/react'
import PrecioCOP from './PrecioCOP'

describe('PrecioCOP', () => {
    it('TEST-FE-LIB-005: formatea 250000 como pesos colombianos', () => {
        render(<PrecioCOP valor={250000} />)
        // Intl separa "$" y la cifra con un espacio duro (U+00A0): \s lo cubre, un espacio normal no
        expect(screen.getByText(/\$\s?250\.000/)).toBeInTheDocument()
    })
})
