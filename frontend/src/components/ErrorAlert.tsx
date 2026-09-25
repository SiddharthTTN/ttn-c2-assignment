interface ErrorAlertProps {
  title?: string
  message: string
  details?: string[]
}

export function ErrorAlert({ title = 'Something went wrong', message, details }: ErrorAlertProps) {
  return (
    <div className="error-alert" role="alert">
      <p className="error-alert__title">{title}</p>
      <p>{message}</p>
      {details && details.length > 0 ? (
        <ul>
          {details.map((detail) => (
            <li key={detail}>{detail}</li>
          ))}
        </ul>
      ) : null}
    </div>
  )
}
