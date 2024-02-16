package at.co.schwaerzler.maximilian.androidmailer

class AndroidMailer private constructor(
    val from: String?,
    val to: String?
) {

    private constructor(builder: Builder) : this(
        builder.from,
        builder.to
    )

    class Builder {
        var from: String? = null
            private set

        var to: String? = null
            private set


        fun from(from: String) = apply {
            this.from = from
        }

        fun to(to: String) = apply {
            this.to = to
        }

        fun build() = AndroidMailer(this)
    }
}