import { forwardRef } from 'react'

interface AnswerCardProps {
  answer: string
}

export const AnswerCard = forwardRef<HTMLHeadingElement, AnswerCardProps>(
  function AnswerCard({ answer }, ref) {
    return (
      <section className="answer-card" aria-labelledby="answer-heading">
        <h2 id="answer-heading" className="h2" tabIndex={-1} ref={ref}>
          Answer
        </h2>
        <p>{answer}</p>
      </section>
    )
  },
)
